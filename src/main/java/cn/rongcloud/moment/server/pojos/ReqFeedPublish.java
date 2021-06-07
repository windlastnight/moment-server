package cn.rongcloud.moment.server.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class ReqFeedPublish {
    @Min(value = 0, message = "type error, range: 0-3")
    @Max(value = 3, message = "type error, range: 0-3")
    private Integer type;

    @NotBlank(message = "content cannot be empty")
    private String content;

    @JsonProperty("org_ids")
    @NotEmpty(message = "orgIds cannot be empty")
    private List<String> orgIds;
}
