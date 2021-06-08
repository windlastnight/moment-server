package cn.rongcloud.moment.server.common.utils;

import cn.rongcloud.moment.server.common.jwt.JwtUser;

/**
 * @author renchaoyang
 * @date 2021/6/7
 */
public class UserHolder {

    public static final ThreadLocal<JwtUser> userHolder = new ThreadLocal();

    public static void setUser(JwtUser jwtUser){
        userHolder.set(jwtUser);
    }

    public static JwtUser getUser() {
        return userHolder.get();
    }

    public static String getUid() {
        return userHolder.get().getUserId();
    }

    public static void clear(){
        userHolder.remove();
    }

}
