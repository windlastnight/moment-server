package cn.rongcloud.moment.server.provider;

import org.springframework.stereotype.Service;

@Service
public class UserAuthProviderImpl implements UserAuthProvider {
    @Override
    public boolean doCredentialsMatch(String userId, String token, String extra) {
        //TODO 鉴权用户身份是否合法
        return true;
    }
}
