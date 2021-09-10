package cn.rongcloud.moment.server.service.asyncTask;

import cn.rongcloud.moment.server.common.im.IMHelper;
import cn.rongcloud.moment.server.common.redis.RedisKey;
import cn.rongcloud.moment.server.common.redis.RedisOptService;
import cn.rongcloud.moment.server.enums.MessageStatus;
import cn.rongcloud.moment.server.enums.MomentsCommentType;
import cn.rongcloud.moment.server.model.*;
import cn.rongcloud.moment.server.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class PublishCommentTask {

    @Autowired
    private IMHelper imHelper;

    @Autowired
    private RedisOptService redisOptService;

    @Autowired
    private MessageService messageService;

    @Async
    public void commentTask(Comment comment, List<String> receivers){
        log.info("async publish comment, commentId:{}", comment.getCommentId());
        CommentNotifyData commentNotifyData = new CommentNotifyData();
        BeanUtils.copyProperties(comment, commentNotifyData);
        commentNotifyData.setCreateDt(comment.getCreateDt().getTime());
        this.imHelper.publishCommentNtf(receivers, commentNotifyData, MomentsCommentType.COMMENT);

        if (receivers != null && !receivers.isEmpty()) {
            List<Message> messages = new ArrayList<>();
            for (String receiverId: receivers) {
                if (receiverId.equals(comment.getUserId())) {
                    continue;
                }
                Message message = new Message();
                message.setFeedId(comment.getFeedId());
                message.setMessageId(comment.getCommentId());
                message.setPublishUserId(comment.getUserId());
                message.setUserId(receiverId);
                message.setCreateDt(comment.getCreateDt());
                message.setMessageType(MomentsCommentType.COMMENT.getType());
                message.setStatus(MessageStatus.NORMAL.getValue());
                messages.add(message);
                redisOptService.zsAdd(RedisKey.getUserUnreadMessageKey(receiverId), message, comment.getCreateDt().getTime());
            }

            messageService.saveMessage(messages);
        }
    }

    @Async
    public void likeTask(Like like, List<String> receivers){
        log.info("async publish like, likeId:{}", like.getLikeId());
        LikeNotifyData likeNotifyData = new LikeNotifyData();
        BeanUtils.copyProperties(like, likeNotifyData);
        likeNotifyData.setCreateDt(like.getCreateDt().getTime());
        this.imHelper.publishCommentNtf(receivers, likeNotifyData, MomentsCommentType.LIKE);

        if (receivers != null && !receivers.isEmpty()) {

            List<Message> messages = new ArrayList<>();
            for (String receiverId: receivers) {
                if (receiverId.equals(like.getUserId())) {
                    continue;
                }
                Message message = new Message();
                message.setFeedId(like.getFeedId());
                message.setMessageId(like.getLikeId());
                message.setUserId(receiverId);
                message.setPublishUserId(like.getUserId());
                message.setCreateDt(like.getCreateDt());
                message.setMessageType(MomentsCommentType.LIKE.getType());
                message.setStatus(MessageStatus.NORMAL.getValue());
                messages.add(message);
                redisOptService.zsAdd(RedisKey.getUserUnreadMessageKey(receiverId), message, like.getCreateDt().getTime());
            }
            messageService.saveMessage(messages);
        }
    }
}
