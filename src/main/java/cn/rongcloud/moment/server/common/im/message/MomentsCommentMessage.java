package cn.rongcloud.moment.server.common.im.message;

import io.rong.messages.BaseMessage;
import io.rong.util.GsonUtil;
import lombok.Data;

@Data
public class MomentsCommentMessage<T> extends BaseMessage {

    @Override
    public String getType() {
        return "RCE:MomentsComment";
    }

    private T data;

    private Integer commentType;

    private Long delayPullTime;

    @Override
    public String toString() {
        return GsonUtil.toJson(this, MomentsCommentMessage.class);
    }

}
