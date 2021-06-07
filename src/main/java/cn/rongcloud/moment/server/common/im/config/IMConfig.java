package cn.rongcloud.moment.server.common.im.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "im")
public class IMConfig {
    private String appKey;
    private String secret;
    private String host;
}
