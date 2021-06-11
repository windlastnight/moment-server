package cn.rongcloud.moment.server.common.im.message;

import io.rong.messages.BaseMessage;
import io.rong.util.GsonUtil;

public class MomentsCommentMessage extends BaseMessage {

    @Override
    public String getType() {
        return "RC:MomentsComment";
    }

    @Override
    public String toString() {
        return GsonUtil.toJson(this, MomentsCommentMessage.class);
    }

}
