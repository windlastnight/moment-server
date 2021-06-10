package cn.rongcloud.moment.server.common.rce;

import cn.rongcloud.moment.server.common.rce.config.RceConfig;
import cn.rongcloud.moment.server.common.rest.RestException;
import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.rest.RestResultCode;
import cn.rongcloud.moment.server.common.utils.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class RceHelper {

    @Autowired
    private RceConfig rceConfig;

    public RceRespResult auth(String userId, String cookie) {
        try {
            HttpURLConnection conn = null;
            conn = HttpUtil.CreateHttpConnection(rceConfig.getHost(), rceConfig.getSystemUid(), rceConfig.getSecret(), "/user/auth");
            conn.setRequestMethod("POST");

            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("user_id", userId);
            requestMap.put("cookie", cookie);

            HttpUtil.setBodyParameter(GsonUtil.toJson(requestMap), conn);
            return (RceRespResult) GsonUtil.fromJson(HttpUtil.returnResult(conn), RceRespResult.class);
        } catch (Exception e) {
            log.info("failed to auth from rce, error:{}", e.getMessage());
            throw new RestException(RestResult.generic(RestResultCode.ERR_CALL_RCE_FAILED));
        }
    }

    public RceRespResult queryAllStaffId(List<String> orgIds, String userId) {
        try {
            HttpURLConnection conn = null;
            conn = HttpUtil.CreateHttpConnection(rceConfig.getHost(), rceConfig.getSystemUid(), rceConfig.getSecret(), "/organization/staffIds");
            conn.setRequestMethod("POST");
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("org_ids", orgIds);
            requestMap.put("user_id", userId);
            HttpUtil.setBodyParameter(GsonUtil.toJson(requestMap), conn);
            return (RceRespResult) GsonUtil.fromJson(HttpUtil.returnResult(conn), RceRespResult.class);
        } catch (Exception e) {
            log.info("failed to get staffIds from rce, error:{}", e.getMessage());
            throw new RestException(RestResult.generic(RestResultCode.ERR_CALL_RCE_FAILED));
        }
    }

    public RceRespResult queryStaffOrgIds(String userId) {
        try {
            HttpURLConnection conn = null;
            conn = HttpUtil.CreateHttpConnection(rceConfig.getHost(), rceConfig.getSystemUid(), rceConfig.getSecret(), "/staffs/" + userId + "/orgIds");
            conn.setRequestMethod("GET");
            return (RceRespResult) GsonUtil.fromJson(HttpUtil.returnResult(conn), RceRespResult.class);
        } catch (Exception e) {
            log.info("failed to query staff orgIds from rce, error:{}", e.getMessage());
            throw new RestException(RestResult.generic(RestResultCode.ERR_CALL_RCE_FAILED));
        }
    }
}
