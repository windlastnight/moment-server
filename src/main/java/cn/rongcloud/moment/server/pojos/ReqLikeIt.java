package cn.rongcloud.moment.server.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author renchaoyang
 * @date 2021/6/9
 */
@Data
public class ReqLikeIt {

    @NotBlank
    @JsonProperty("feed_id")
    private String feedId;

}
