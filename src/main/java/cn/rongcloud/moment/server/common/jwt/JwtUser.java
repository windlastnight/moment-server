package cn.rongcloud.moment.server.common.jwt;

import lombok.Data;

@Data
public class JwtUser {
    private String userId;
    private String cookie;
    private String token;
    private String extra;
}
