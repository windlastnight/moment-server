package cn.rongcloud.moment.server.pojos;

import lombok.Data;

/**
 * @author renchaoyang
 * @date 2021/6/7
 */
@Data
public class RespAuth {
    private String authorization;
    private long expire;
}
