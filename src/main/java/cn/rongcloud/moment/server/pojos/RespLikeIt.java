package cn.rongcloud.moment.server.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author renchaoyang
 * @date 2021/6/9
 */
@Data
public class RespLikeIt {

    @JsonProperty("like_id")
    private String likeId;

    @JsonProperty("create_dt")
    private Date createDt;

}
