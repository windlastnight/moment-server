package cn.rongcloud.moment.server.common.config;

import cn.rongcloud.moment.server.common.utils.ApplicationUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author renchaoyang
 * @date 2021/6/18
 */
@Component
public class SysConfig {

    @Bean
    ApplicationUtil getApplicationUtils() {
        return new ApplicationUtil();
    }


}
