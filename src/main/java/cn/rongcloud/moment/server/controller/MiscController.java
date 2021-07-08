package cn.rongcloud.moment.server.controller;

import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.pojos.MomentVersion;
import cn.rongcloud.moment.server.pojos.OutputVersion;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by sunyinglong on 2020/7/30
 */
@RestController
@RequestMapping("/misc")
@Slf4j
public class MiscController {

    private static Logger logger = LoggerFactory.getLogger(MiscController.class);

    @Autowired
    private MomentVersion momentVersion;

    @RequestMapping("/version")
    public RestResult buildVersion() {
        logger.info("buildVersion");
        OutputVersion outputVersion = new OutputVersion();
        outputVersion.setBuildBranch(momentVersion.getBuildBranch());
        outputVersion.setBuildVersion(momentVersion.getBuildVersion());
        outputVersion.setBuildCommitId(momentVersion.getBuildCommitId());
        outputVersion.setBuildTime(momentVersion.getBuildTime());
        return RestResult.success(outputVersion);
    }

}
