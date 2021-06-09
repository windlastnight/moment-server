package cn.rongcloud.moment.server.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Date;

@Data
public class RespFeedPublish {

    @JsonProperty("feed_id")
    private String feedId;

    @JsonProperty("status")
    private Integer feedStatus;

    @JsonProperty("create_dt")
    private Date createDt;

    @JsonProperty("update_dt")
    private Date updateDt;

}
