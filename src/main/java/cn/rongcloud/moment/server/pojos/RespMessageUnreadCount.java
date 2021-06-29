package cn.rongcloud.moment.server.pojos;

import cn.rongcloud.moment.server.model.Message;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RespMessageUnreadCount {
    private Long count;

    @JsonProperty("latest_message")
    private Message latestMessage;
}
