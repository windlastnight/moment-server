package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.rest.RestResultCode;

/**
 * @author renchaoyang
 * @date 2021/6/11
 */
public interface CoverService {
    void setCover(String cover);

    RestResult getCover();
}
