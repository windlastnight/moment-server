package cn.rongcloud.moment.server.common.im.message;

import io.rong.messages.BaseMessage;
import io.rong.util.GsonUtil;

public class MomentsUpdatedMessage extends BaseMessage {

    @Override
    public String getType() {
        return "RC:MomentsUpdate";
    }

    @Override
    public String toString() {
        return GsonUtil.toJson(this, MomentsUpdatedMessage.class);
    }

}
