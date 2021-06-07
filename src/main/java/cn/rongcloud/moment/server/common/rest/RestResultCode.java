package cn.rongcloud.moment.server.common.rest;

/**
 * Created by sunyinglong on 2020/7/6
 */
public enum RestResultCode {

    // 通用错误码
    ERR_SUCCESS(10000, "OK"),
    ERR_OTHER(10001, "Error"),
    ERR_REQUEST_PARA_ERR(10002, "Missing or invalid parameter"),
    ERR_INVALID_AUTH(10003, "Invalid or expired authorization"),
    ERR_ACCESS_DENIED(10004, "Access denied"),
    ERR_BAD_REQUEST(10005, "Bad request"),
    ERR_LOGIN_AUTH_FAILED(10006, "login auth failed"),

    ERR_FEED_PUBLISH_ORG_ID_ERROR(20000, "The org not existed or you don't have permission"),
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
