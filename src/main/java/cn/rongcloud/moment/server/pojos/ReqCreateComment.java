package cn.rongcloud.moment.server.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author renchaoyang
 * @date 2021/6/9
 */
@Data
public class ReqCreateComment {

    @NotBlank
    @JsonProperty("feed_id")
    private String feedId;

    @NotBlank
    @JsonProperty("content")
    private String commentContent;

    @JsonProperty("reply_to")
    private String replyTo;


}
