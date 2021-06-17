package cn.rongcloud.moment.server.common.rest;

/**
 * Created by sunyinglong on 2020/7/6
 */
public enum RestResultCode {

    // 通用错误码
    ERR_SUCCESS(20000, "OK"),
    ERR_OTHER(20001, "Error"),
    ERR_REQUEST_PARA_ERR(20002, "Missing or invalid parameter"),
    ERR_INVALID_AUTH(20003, "Invalid or expired authorization"),
    ERR_ACCESS_DENIED(20004, "Access denied"),
    ERR_BAD_REQUEST(20005, "Bad request"),
    ERR_LOGIN_AUTH_FAILED(20006, "login auth failed"),
    ERR_CALL_RCE_FAILED(20007, "req failed to rce"),
    ERR_REQ_WITHOUT_REQIRED_AUTHORIZATION_HEADER(20008, "req lack required header authorization"),

    ERR_FEED_PUBLISH_ORG_ID_ERROR(20100, "The org not existed or you don't have permission"),
    ERR_FEED_NOT_EXISTED(20101, "The feed does not existed"),

    ERR_COMMENT_NOT_EXISTED(20102, "The comment does not existed"),
    ERR_LIKE_USER_ALEADY_LIKED(20103, "user liked it"),
    ERR_LIKE_USER_NO_LIKE(20104, "user have no like it"),
    ERR_MESSAGE_NOT_EXISTED(20105, "The messageId does not existed"),
    ;

    private int code;
    private String msg;

    RestResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }
}
