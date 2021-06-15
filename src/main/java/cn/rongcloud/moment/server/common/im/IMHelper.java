package cn.rongcloud.moment.server.common.im;

import cn.rongcloud.moment.server.common.CustomerConstant;
import cn.rongcloud.moment.server.common.im.config.IMConfig;
import cn.rongcloud.moment.server.common.im.message.MomentsCommentMessage;
import cn.rongcloud.moment.server.common.im.message.MomentsUpdatedMessage;
import cn.rongcloud.moment.server.enums.MomentsCommentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class IMHelper {

    @Autowired
    private IMConfig imConfig;

    public void publishFeedNtf(List<String> staffIds) {

        MomentsUpdatedMessage message = new MomentsUpdatedMessage();

        IMRequestQueue.messageBuilder(CustomerConstant.SYSTEM_ID, ConversationType.SYSTEM, staffIds, message)
                .persist(0)
                .includeSender(0)
                .imConfig(imConfig)
                .buildAndSend();
    }

    public <T> void publishCommentNtf(List<String> staffIds, T content, MomentsCommentType commentType) {

        MomentsCommentMessage message = new MomentsCommentMessage();
        message.setData(content);
        message.setCommentType(commentType.getType());

        IMRequestQueue.messageBuilder(CustomerConstant.SYSTEM_ID, ConversationType.SYSTEM, staffIds, message)
                .persist(0)
                .includeSender(0)
                .imConfig(imConfig)
                .buildAndSend();
    }
}
