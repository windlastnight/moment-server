package cn.rongcloud.moment.server.common.rest;

import lombok.extern.slf4j.Slf4j;

/**
 * 自定义异常处理
 */
@Slf4j
public class RestException extends RuntimeException {

    private final transient RestResult restResult;

    public RestException(RestResult restResult) {
        this.restResult = restResult;

        log.error("RestException: {}", restResult, this);
    }

    public RestResult getRestResult() {
        return this.restResult;
    }
}
