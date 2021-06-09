package cn.rongcloud.moment.server.controller;

import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.utils.GsonUtil;
import cn.rongcloud.moment.server.pojos.ReqAuth;
import cn.rongcloud.moment.server.service.AuthService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * Created by sunyinglong on 2020/7/30
 */

@RestController
@RequestMapping("/user")
@Slf4j
public class AuthController {

    @Resource
    AuthService authService;

    @ApiOperation(value = "用户认证")
    @PostMapping("/auth")
    public RestResult authorize(@Valid @RequestBody ReqAuth reqAuth) {
        log.info("user auth, data:{}", GsonUtil.toJson(reqAuth));
        return authService.doAuth(reqAuth);
    }

}
