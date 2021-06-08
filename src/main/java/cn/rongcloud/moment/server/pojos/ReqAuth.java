package cn.rongcloud.moment.server.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author renchaoyang
 * @date 2021/6/7
 */
@Data
@ApiModel
public class ReqAuth {

    @NotBlank
    @JsonProperty("user_id")
    @ApiModelProperty(required = true, name = "user_id", example = "ZpWkCZTlSdjIvdpfPdstQFB")
    private String userId;

    @NotBlank
    @ApiModelProperty(required = true, name = "cookie", example = "zpVtOyBwT1cgyxSpL3jVaY")
    private String cookie;
}
