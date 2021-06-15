package cn.rongcloud.moment.server.service;


import cn.rongcloud.moment.server.common.redis.RedisKey;
import cn.rongcloud.moment.server.common.redis.RedisOptService;
import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.rest.RestResultCode;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.enums.MessageStatus;
import cn.rongcloud.moment.server.enums.MomentsCommentType;
import cn.rongcloud.moment.server.mapper.MessageMapper;
import cn.rongcloud.moment.server.model.Comment;
import cn.rongcloud.moment.server.model.Message;
import cn.rongcloud.moment.server.pojos.RespMessageInfo;
import cn.rongcloud.moment.server.pojos.RespMessageUnreadCount;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by sunyinglong on 2020/6/3
 */
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private RedisOptService optService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Override
    public void saveMessage(Message message) {
        messageMapper.insertMessage(message);
    }

    @Override
    public RestResult getUnreadCount() {
        Long unreadSize = optService.zsSize(RedisKey.getUserUnreadMessageKey(UserHolder.getUid()));
        RespMessageUnreadCount resp = new RespMessageUnreadCount();
        resp.setCount(unreadSize);
        return RestResult.success(resp);
    }

    @Override
    public RestResult getUnread() {

        List<Message> messages = (List<Message>) optService.zReverseRangeByScore(RedisKey.getUserUnreadMessageKey(UserHolder.getUid()), 0, -1);

        List<RespMessageInfo> resp = buildRespMessage(messages);

        return RestResult.success(resp);
    }

    @Override
    public RestResult getHistory(String fromMessageId, Integer size) {

        Long fromMessageAutoIncId = null;
        if (!StringUtils.isEmpty(fromMessageId)) {
            Message message = messageMapper.getMessage(fromMessageId);
            if (message == null) {
                return RestResult.generic(RestResultCode.ERR_MESSAGE_NOT_EXISTED);
            }
            fromMessageAutoIncId = message.getId();
        }

        List<Message> messages = messageMapper.getMessageByPage(UserHolder.getUid(), fromMessageAutoIncId, size);
        List<RespMessageInfo> resp = buildRespMessage(messages);

        return RestResult.success(resp);
    }

    @Override
    public RestResult batchDelete(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return RestResult.success();
        }
        messageMapper.batchDelete(UserHolder.getUid(), ids);
        return RestResult.success();
    }

    @Override
    public RestResult deleteAll() {
        messageMapper.deleteAll(UserHolder.getUid());
        return RestResult.success();
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
            respMessageInfo.setUserId(message.getUserId());
            respMessageInfo.setStatus(message.getStatus());
            respMessageInfo.setType(message.getMessageType());
            respMessageInfo.setCreateDt(message.getCreateDt());
            if (message.getMessageType() == MomentsCommentType.COMMENT.getType()
                    && commentMap.containsKey(message.getMessageId())
                    && message.getStatus() == MessageStatus.NORMAL.getValue()) {
                Comment comment = commentMap.get(message.getMessageId());
                respMessageInfo.setReplyTo(comment.getReplyTo());
                respMessageInfo.setCommentContent(comment.getCommentContent());
            }
            resp.add(respMessageInfo);
        }
        return resp;
    }
}
