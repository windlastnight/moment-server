package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.jwt.JwtTokenHelper;
import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.provider.UserAuthProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by sunyinglong on 2020/6/3
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserAuthProvider userAuthProvider;

    @Autowired
    JwtTokenHelper jwtTokenHelper;

    @Override
    public RestResult login() {


        return null;
    }
}
