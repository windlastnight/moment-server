package cn.rongcloud.moment.server.provider;

public interface UserAuthProvider {
    /**
     * 用户信息认证
     * @param userId 用户 Id
     * @param token 用户认证 token
     * @param extra 扩展参数
     * @return
     */
    boolean doCredentialsMatch(String userId, String token, String extra);
}
