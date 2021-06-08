package cn.rongcloud.moment.server.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author renchaoyang
 * @date 2021/6/8
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "moment.pass")
public class PassUrls {

    List<String> anonymousUrls;

}
