package cn.rongcloud.moment.server.service;


import cn.rongcloud.moment.server.common.redis.RedisKey;
import cn.rongcloud.moment.server.common.redis.RedisOptService;
import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.rest.RestResultCode;
import cn.rongcloud.moment.server.common.utils.GsonUtil;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.enums.MessageStatus;
import cn.rongcloud.moment.server.enums.MomentsCommentType;
import cn.rongcloud.moment.server.model.Comment;
import cn.rongcloud.moment.server.model.Like;
import cn.rongcloud.moment.server.model.Message;
import cn.rongcloud.moment.server.pojos.RespMessageInfo;
import cn.rongcloud.moment.server.pojos.RespMessageUnreadCount;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by sunyinglong on 2020/6/3
 */
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private RedisOptService optService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Override
    public void saveMessage(List<Message> messages) {
        elasticSearchService.batchSaveMessage(messages);
    }

    @Override
    public RestResult getUnreadCount() {
        Long unreadSize = optService.zsSize(RedisKey.getUserUnreadMessageKey(UserHolder.getUid()));
        RespMessageUnreadCount resp = new RespMessageUnreadCount();
        resp.setCount(unreadSize);
        if (unreadSize != null && unreadSize != 0) {
            Set<Message> message = (Set<Message>) optService.zReverseRange(RedisKey.getUserUnreadMessageKey(UserHolder.getUid()), 0, 0);
            if (message != null && !message.isEmpty()) {
                List<RespMessageInfo> messages = buildRespMessage(new ArrayList<>(message));
                resp.setLatestMessage(messages.get(0));
            }
        }
        return RestResult.success(resp);
    }

    @Override
    public RestResult getUnread() {

        Set<Message> messages = (Set<Message>) optService.zsAll(RedisKey.getUserUnreadMessageKey(UserHolder.getUid()));
        optService.deleteKey(RedisKey.getUserUnreadMessageKey(UserHolder.getUid()));
        List<Message> messageList = new ArrayList<>(messages);
        if (!messageList.isEmpty() && messageList.size() > 1000) {
            messageList = messageList.subList(0, 1000);
        }
        List<RespMessageInfo> resp = buildRespMessage(messageList);
        Collections.reverse(resp);
        return RestResult.success(resp);
    }

    @Override
    public RestResult getHistory(String fromMessageId, Integer size) {

        Message message = null;
        if (!StringUtils.isEmpty(fromMessageId)) {
            message = elasticSearchService.getMessage(fromMessageId, UserHolder.getUid());
            if (message == null) {
                return RestResult.generic(RestResultCode.ERR_MESSAGE_NOT_EXISTED);
            }
        } else {
            optService.deleteKey(RedisKey.getUserUnreadMessageKey(UserHolder.getUid()));
        }

        List<Message> messages = elasticSearchService.getMessageByPage(UserHolder.getUid(), size, message);

        List<RespMessageInfo> resp = buildRespMessage(messages);

        return RestResult.success(resp);
    }

    @Override
    public RestResult batchDelete(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return RestResult.success();
        }
        Map<String, Date> map = new HashMap<>();
        List<Like> likes = likeService.batchGetLikes(ids);
        log.info("likes:{}", GsonUtil.toJson(likes));
        if (likes != null) {
           map.putAll(likes.stream().collect(Collectors.toMap(Like::getLikeId, Like::getCreateDt)));
        }
        if (likes != null && likes.size() != ids.size()) {
            List<Comment> comments = commentService.batchGetComment(ids);
            log.info("comments:{}", GsonUtil.toJson(comments));
            if (comments != null) {
                map.putAll(comments.stream().collect(Collectors.toMap(Comment::getCommentId, Comment::getCreateDt)));
            }
        }
        log.info("test:{}", GsonUtil.toJson(map));
        elasticSearchService.deleteByMessageIds(map);
        return RestResult.success();
    }

    @Override
    public RestResult deleteAll() {
        elasticSearchService.deleteByUserId(UserHolder.getUid());
        return RestResult.success();
    }

    @Override
    public List<String> getLikeAlreadyNotifyUser(String messageId, String userId) {
        return elasticSearchService.getLikeAlreadyNotifyUser(messageId, userId);
    }

    private List<RespMessageInfo> buildRespMessage(List<Message> messages) {

        List<RespMessageInfo> resp = new ArrayList<>();

        if (messages == null || messages.isEmpty()) {
            return resp;
        }

        Map<String, Comment> commentMap = new HashMap<>();
        List<String> commentIds = new ArrayList<>();
        for (Message message: messages) {
            if (message.getMessageType() == MomentsCommentType.COMMENT.getType()) {
                commentIds.add(message.getMessageId());
            }
        }
        if (!commentIds.isEmpty()) {
            List<Comment> comments = commentService.batchGetComment(commentIds);
            commentMap = comments.stream().collect(Collectors.toMap(Comment::getCommentId, Function.identity()));
        }

        for (Message message: messages) {
            RespMessageInfo respMessageInfo = new RespMessageInfo();
            respMessageInfo.setFeedId(message.getFeedId());
            respMessageInfo.setMessageId(message.getMessageId());
            respMessageInfo.setUserId(message.getPublishUserId());
            respMessageInfo.setStatus(message.getStatus());
            respMessageInfo.setType(message.getMessageType());
            respMessageInfo.setCreateDt(message.getCreateDt());
            if (message.getMessageType() == MomentsCommentType.COMMENT.getType()) {
                if (commentMap.containsKey(message.getMessageId())) {
                    Comment comment = commentMap.get(message.getMessageId());
                    respMessageInfo.setReplyTo(comment.getReplyTo());
                    respMessageInfo.setCommentContent(comment.getCommentContent());
                } else {
                    respMessageInfo.setStatus(MessageStatus.DELETED.getValue());
                }
            }
            resp.add(respMessageInfo);
        }
        return resp;
    }
}
