package cn.rongcloud.moment.server.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class RespMessageInfo {

    @JsonProperty("message_id")
    private String messageId;

    private Integer type;

    @JsonProperty("feed_id")
    private String feedId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("comment_content")
    private String commentContent;

    @JsonProperty("reply_to")
    private String replyTo;

    private Integer status;

    @JsonProperty("create_dt")
    private Date createDt;
}
