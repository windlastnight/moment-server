package cn.rongcloud.moment.server.pojos;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author Created by CZN on 2017/3/31.
 */
@Configuration
@PropertySource("classpath:git.properties")
@Data
public class MomentVersion {

    @Value("${git.build.time}")
    private String buildTime;

    @Value("${git.build.version}")
    private String buildVersion;

    @Value("${git.commit.id}")
    private String buildCommitId;

    @Value("${git.branch}")
    private String buildBranch;
}
