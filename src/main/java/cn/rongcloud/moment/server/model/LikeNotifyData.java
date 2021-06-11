package cn.rongcloud.moment.server.model;

import lombok.Data;

import java.util.Date;

/**
 * @author renchaoyang
 * @date 2021/6/11
 */
@Data
public class LikeNotifyData {
    private Long id;

    private String likeId;

    private String feedId;

    private String userId;

    private Long createDt;
}
