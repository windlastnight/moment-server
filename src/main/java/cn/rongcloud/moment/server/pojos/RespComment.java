package cn.rongcloud.moment.server.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author renchaoyang
 * @date 2021/6/9
 */
@Data
public class RespComment {

    @JsonProperty("comment_id")
    private String commentId;

    @JsonProperty("feed_id")
    private String feedId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("content")
    private String commentContent;

    @JsonProperty("reply_to")
    private String replyTo;

    @JsonProperty("create_dt")
    private Date createDt;
}
