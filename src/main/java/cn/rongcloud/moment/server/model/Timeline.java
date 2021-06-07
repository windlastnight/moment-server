package cn.rongcloud.moment.server.model;

import lombok.Data;

import java.util.Date;

@Data
public class Timeline {
    private String feedId;
    private String orgId;
    private Date createDt;
}
