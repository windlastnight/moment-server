package cn.rongcloud.moment.server.common.im;

import cn.rongcloud.moment.server.common.CustomerConstant;
import cn.rongcloud.moment.server.common.im.config.IMConfig;
import cn.rongcloud.moment.server.common.im.message.MomentsCommentMessage;
import cn.rongcloud.moment.server.common.im.message.MomentsUpdatedMessage;
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

    public void publishCommentNtf(List<String> staffIds) {

        MomentsCommentMessage message = new MomentsCommentMessage();

        IMRequestQueue.messageBuilder(CustomerConstant.SYSTEM_ID, ConversationType.SYSTEM, staffIds, message)
                .persist(0)
                .includeSender(0)
                .imConfig(imConfig)
                .buildAndSend();
    }
}
