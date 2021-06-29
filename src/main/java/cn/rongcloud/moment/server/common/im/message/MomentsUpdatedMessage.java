package cn.rongcloud.moment.server.common.im.message;

import io.rong.messages.BaseMessage;
import io.rong.util.GsonUtil;
import lombok.Data;

@Data
public class MomentsUpdatedMessage extends BaseMessage {

    @Override
    public String getType() {
        return "RCE:MomentsUpdate";
    }

    private Long delayPullTime;

    @Override
    public String toString() {
        return GsonUtil.toJson(this, MomentsUpdatedMessage.class);
    }

}
