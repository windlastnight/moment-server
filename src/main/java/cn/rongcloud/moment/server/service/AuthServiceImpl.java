package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.jwt.JwtToken;
import cn.rongcloud.moment.server.common.jwt.JwtTokenHelper;
import cn.rongcloud.moment.server.common.jwt.JwtUser;
import cn.rongcloud.moment.server.common.jwt.config.JwtProperties;
import cn.rongcloud.moment.server.common.rce.RceHelper;
import cn.rongcloud.moment.server.common.rce.RceRespResult;
import cn.rongcloud.moment.server.common.rest.RestException;
import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.rest.RestResultCode;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.pojos.ReqAuth;
import cn.rongcloud.moment.server.pojos.RespAuth;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author renchaoyang
 * @date 2021/6/7
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private JwtTokenHelper tokenHelper;

    @Resource
    private JwtProperties jwtProperties;

    @Resource
    private RceHelper rceHelper;

    @Override
    public RestResult doAuth(ReqAuth auth) throws RestException {

        RceRespResult result = rceHelper.auth(auth.getUserId(), auth.getCookie());
        if (!result.isSuccess()) {
            return RestResult.generic(RestResultCode.ERR_LOGIN_AUTH_FAILED);
        }

        return RestResult.success(this.generateToken(auth));
    }

    @Override
    public void checkAuth(String token){
        try {
            JwtUser jwtUser = this.tokenHelper.checkJwtToken(token);
            UserHolder.setUser(jwtUser);
        } catch (IOException e) {
            throw new RestException(RestResult.generic(RestResultCode.ERR_LOGIN_AUTH_FAILED));
        } catch (ExpiredJwtException e){
            throw new RestException(RestResult.generic(RestResultCode.ERR_INVALID_AUTH));
        }
    }

    private RespAuth generateToken(ReqAuth auth) {
        JwtUser jwtUser = new JwtUser();
        jwtUser.setUserId(auth.getUserId());
        jwtUser.setCookie(auth.getCookie());
        JwtToken jwtToken = null;
        jwtToken = this.tokenHelper.createJwtToken(jwtUser);

        RespAuth respAuth = new RespAuth();
        respAuth.setAuthorization(jwtToken.getToken());
        respAuth.setExpire(jwtToken.getExpiredTime());
        return respAuth;
    }

}
