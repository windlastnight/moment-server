package cn.rongcloud.moment.server.common.rce.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "rce")
public class RceConfig {
    private String systemUid;
    private String secret;
    private String host;
}
