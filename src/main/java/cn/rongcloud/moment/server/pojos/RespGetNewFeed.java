package cn.rongcloud.moment.server.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RespGetNewFeed {

    @JsonProperty("has_new")
    private boolean hasNew;
}
