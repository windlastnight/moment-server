package cn.rongcloud.moment.server.common.utils;

import cn.rongcloud.moment.server.common.im.config.IMProperties;
import io.rong.RongCloud;
import io.rong.RongCloudConfig;
import org.apache.commons.lang3.StringUtils;

/**
 * @author renchaoyang
 * @date 2021/6/1
 */
public class RongCloudUtil {

    public static RongCloud getRongCloud() {
        IMProperties imProp = ApplicationUtil.getBean(IMProperties.class);
        return StringUtils.isBlank(imProp.getHost()) ? RongCloud.getInstance(imProp.getAppKey(), imProp.getSecret())
                : RongCloud.getInstance(imProp.getAppKey(), imProp.getSecret(), new RongCloudConfig(imProp.getHost()));
    }

}
