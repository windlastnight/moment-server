package cn.rongcloud.moment.server.controller;

import cn.rongcloud.moment.server.common.jwt.JwtUser;
import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.pojos.ReqAuth;
import cn.rongcloud.moment.server.pojos.RespAuth;
import cn.rongcloud.moment.server.service.AuthService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * Created by sunyinglong on 2020/7/30
 */

@RestController
@RequestMapping("/moment")
@Slf4j
public class AuthController {


    @Resource
    HttpServletRequest req;

    @Resource
    AuthService authService;

    @ApiOperation(value = "用户认证")
    @PostMapping("/auth")
    public RestResult authorize(@Valid @RequestBody ReqAuth reqAuth) {
        RespAuth token = this.authService.doAuth(reqAuth);
        return RestResult.success(token);
    }

    @GetMapping("/test")
    public RestResult test() {
        JwtUser user = UserHolder.getUser();
//        RespAuth token = this.authService.doAuth(reqAuth);
        return RestResult.success();
    }



}
