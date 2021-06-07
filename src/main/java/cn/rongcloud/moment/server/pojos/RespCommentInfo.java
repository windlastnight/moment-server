package cn.rongcloud.moment.server.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RespCommentInfo {

    @JsonProperty("comment_id")
    private String commentId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("reply_to")
    private String replyTo;

    @JsonProperty("type")
    private String type;

    @JsonProperty("content")
    private String content;

    @JsonProperty("create_dt")
    private String createDt;
}
