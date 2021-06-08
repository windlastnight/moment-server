package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.rest.RestException;
import cn.rongcloud.moment.server.pojos.ReqAuth;
import cn.rongcloud.moment.server.pojos.RespAuth;

/**
 * @author renchaoyang
 * @date 2021/6/3
 */
public interface AuthService {
    RespAuth doAuth(ReqAuth rceCookie) throws RestException;
    void checkAuth(String token) throws RestException;
}
