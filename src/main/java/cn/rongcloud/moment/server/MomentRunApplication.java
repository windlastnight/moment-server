package cn.rongcloud.moment.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class MomentRunApplication {

    public static void main(String[] args) {
        SpringApplication.run(MomentRunApplication.class, args);
    }

}
