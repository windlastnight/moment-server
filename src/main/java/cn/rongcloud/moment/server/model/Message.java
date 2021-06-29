package cn.rongcloud.moment.server.model;

import lombok.Data;

import java.util.Date;

@Data
public class Message {
    private Long id;
    private String messageId;
    private String feedId;
    private Integer messageType;
    private String publishUserId;
    private String userId;
    private Integer status;
    private Date createDt;
}
