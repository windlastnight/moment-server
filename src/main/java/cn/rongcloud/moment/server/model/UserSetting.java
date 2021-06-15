package cn.rongcloud.moment.server.model;

import lombok.Data;

import java.util.Date;

@Data
public class UserSetting {
    private String userId;

    private String cover;

    private Date updateDt;

}