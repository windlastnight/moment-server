package cn.rongcloud.moment.server.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class RespFeedPublish {

    @JsonProperty("feed_id")
    private String feedId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("type")
    private Integer feedType;

    @JsonProperty("content")
    private String feedContent;

    @JsonProperty("status")
    private Integer feedStatus;

    @JsonProperty("create_dt")
    private Date createDt;

    @JsonProperty("update_dt")
    private Date updateDt;
}
