package cn.rongcloud.moment.server.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author renchaoyang
 * @date 2021/6/9
 */
@Data
public class RespCreateComment {

    @JsonProperty("comment_id")
    private String commentId;

    @JsonProperty("create_dt")
    private String createDt;

    private String content;

}
