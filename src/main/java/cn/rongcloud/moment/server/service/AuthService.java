package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.pojos.ReqAuth;

/**
 * @author renchaoyang
 * @date 2021/6/3
 */
public interface AuthService {
    RestResult doAuth(ReqAuth rceCookie);
    void checkAuth(String token);
}
