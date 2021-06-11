package cn.rongcloud.moment.server.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class RespLike {

    @JsonProperty("like_id")
    private String likeId;

//    private String feedId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("create_dt")
    private Date createDt;

}