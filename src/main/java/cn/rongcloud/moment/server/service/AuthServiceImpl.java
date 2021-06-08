package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.jwt.JwtToken;
import cn.rongcloud.moment.server.common.jwt.JwtTokenHelper;
import cn.rongcloud.moment.server.common.jwt.JwtUser;
import cn.rongcloud.moment.server.common.jwt.config.JwtProperties;
import cn.rongcloud.moment.server.common.rest.RestException;
import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.rest.RestResultCode;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.constant.UriConstant;
import cn.rongcloud.moment.server.pojos.ReqAuth;
import cn.rongcloud.moment.server.pojos.RespAuth;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 * @author renchaoyang
 * @date 2021/6/7
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private RestTemplate restTemplate;

    @Value("${moment.rce.api}")
    private String rceBaseUrl;

    @Resource
    private JwtTokenHelper tokenHelper;

    @Resource
    private JwtProperties jwtProperties;

    @Override
    public RespAuth doAuth(ReqAuth auth) throws RestException {
        RequestEntity req = RequestEntity.post(URI.create(rceBaseUrl + UriConstant.AUTH_URL))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).body(auth);
        try {
            ResponseEntity<RestResult> exchange = this.restTemplate.exchange(req, RestResult.class);
            Optional.ofNullable(exchange).map(ResponseEntity::getBody).map(RestResult::getCode)
                    .filter(code -> Objects.equals(UriConstant.RCE_SUSS_CODE, code))
                    .orElseThrow(() -> new RestException(RestResult.generic(RestResultCode.ERR_LOGIN_AUTH_FAILED)));
        } catch (RestClientException e) {
            throw new RestException(RestResult.generic(RestResultCode.ERR_LOGIN_API_CALL_FAILED));
        }

        return this.generateToken(auth);
    }

    @Override
    public void checkAuth(String token) throws RestException {
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
