package cn.rongcloud.moment.server.model;

import lombok.Data;

import java.util.Date;

@Data
public class Like {
    private Long id;

    private String likeId;

    private String feedId;

    private String userId;

    private Integer likeStatus;

    private Date createDt;

}