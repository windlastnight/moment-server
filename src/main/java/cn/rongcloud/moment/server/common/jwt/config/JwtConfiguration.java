package cn.rongcloud.moment.server.common.jwt.config;

import cn.rongcloud.moment.server.common.jwt.JwtTokenHelper;
import cn.rongcloud.moment.server.common.jwt.filter.CrosFilter;
import cn.rongcloud.moment.server.common.jwt.filter.JwtFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sunyinglong on 2020/6/25
 */
@Slf4j
@Configuration
public class JwtConfiguration {

    @Bean
    public JwtTokenHelper jwtTokenHelper(JwtProperties jwtProperties) {
        log.info("config jwtTokenHelper expired {} ms", jwtProperties.getTtlInMilliSec());
        return jwtProperties.getTtlInMilliSec() == null
                ? new JwtTokenHelper(jwtProperties.getSecret())
                : new JwtTokenHelper(jwtProperties.getSecret(), jwtProperties.getTtlInMilliSec());
    }

    @Bean
    public FilterRegistrationBean jwtTokenFilter(JwtFilter jwtFilter) {
        log.info("config jwtTokenFilter");
        final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(jwtFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(2);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean crosFilter(CrosFilter crosFilter) {
        log.info("config crosFilter");
        final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(crosFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    @Bean
    CrosFilter crosFilter() {
        return new CrosFilter();
    }
}
