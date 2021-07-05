package cn.rongcloud.moment.server.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author renchaoyang
 * @date 2021/6/16
 */
@Data
@Component
@ConfigurationProperties(prefix = "moment.expire")
public class CacheExpireProperties {

    private Long comment;
//    private Long like;
    private Long feed;
    private Long intervalS;

}
