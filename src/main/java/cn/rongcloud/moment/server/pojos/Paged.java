package cn.rongcloud.moment.server.pojos;

import cn.rongcloud.moment.server.common.CustomerConstant;
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
    private String fromUId;

//    @Min(value = 1)
    private Integer size;

    private Long fromId;

    public void setSize(Integer size) {
        if (size == null) {
            size = CustomerConstant.DEF_PAGE_SIZE;
        }
        this.size = size;

    }
}
