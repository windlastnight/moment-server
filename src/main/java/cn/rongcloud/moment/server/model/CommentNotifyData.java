package cn.rongcloud.moment.server.model;

import lombok.Data;

import java.util.Date;

/**
 * @author renchaoyang
 * @date 2021/6/11
 */
@Data
public class CommentNotifyData {
    private Long id;

    private String commentId;

    private String feedId;

    private String userId;

    private String commentContent;

    private String replyTo;

    private Long createDt;
}
