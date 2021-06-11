package cn.rongcloud.moment.server.model;

import lombok.Data;

import java.util.Date;

@Data
public class Comment {
    private String commentId;

    private String feedId;

    private String userId;

    private String commentContent;

    private String replyTo;

    private Date createDt;

}