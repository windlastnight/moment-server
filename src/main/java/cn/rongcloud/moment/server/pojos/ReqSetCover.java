package cn.rongcloud.moment.server.pojos;

import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotBlank;

/**
 * @author renchaoyang
 * @date 2021/6/11
 */
@Data
public class ReqSetCover {
    @NotBlank
    private String cover;
}
