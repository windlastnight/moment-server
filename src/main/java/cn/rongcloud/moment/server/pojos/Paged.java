package cn.rongcloud.moment.server.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author renchaoyang
 * @date 2021/6/9
 */
@Data
public class Paged {

    @JsonProperty("from_comment_id")
    private String fromId;

    @NotNull
    @Min(value = 1)
    private Integer size;
}
