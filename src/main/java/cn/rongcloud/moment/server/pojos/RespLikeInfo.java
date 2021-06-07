package cn.rongcloud.moment.server.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class RespLikeInfo {
    @JsonProperty("like_id")
    private String likeId;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("create_dt")
    private Date createDt;
}
