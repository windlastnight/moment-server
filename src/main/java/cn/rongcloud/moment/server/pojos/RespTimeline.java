package cn.rongcloud.moment.server.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RespTimeline {

    @JsonProperty("feed_ids")
    private List<String> feedIds;
}
