package cn.rongcloud.moment.server.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RespMessageUnreadCount {
    private Long count;

    @JsonProperty("latest_message")
    private RespMessageInfo latestMessage;
}
