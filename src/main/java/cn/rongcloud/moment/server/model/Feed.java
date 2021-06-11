package cn.rongcloud.moment.server.model;

import lombok.Data;

import java.util.Date;

@Data
public class Feed {
    private Long id;
    private String feedId;
    private String userId;
    private Integer feedType;
    private String feedContent;
    private Integer feedStatus;
    private Date createDt;
    private Date updateDt;
}
