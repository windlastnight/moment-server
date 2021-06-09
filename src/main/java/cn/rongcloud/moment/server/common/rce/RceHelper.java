package cn.rongcloud.moment.server.common.rce;

import cn.rongcloud.moment.server.common.rce.config.RceConfig;
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

    public RceQueryResult queryAllStaffId(List<String> orgIds, String userId) {
        try {
            HttpURLConnection conn = null;
            conn = HttpUtil.CreateHttpConnection(rceConfig.getHost(), rceConfig.getSystemUid(), rceConfig.getSecret(), "/organization/staffIds");
            conn.setRequestMethod("POST");
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("org_ids", orgIds);
            requestMap.put("user_id", userId);
            HttpUtil.setBodyParameter(GsonUtil.toJson(requestMap), conn);
            return (RceQueryResult) GsonUtil.fromJson(HttpUtil.returnResult(conn), RceQueryResult.class);
        } catch (Exception e) {
            log.info("failed to get staffIds from rce, error:{}", e.getMessage());
        }
        return null;
    }

    public RceQueryResult queryStaffOrgIds(String userId) {
        try {
            HttpURLConnection conn = null;
            conn = HttpUtil.CreateHttpConnection(rceConfig.getHost(), rceConfig.getSystemUid(), rceConfig.getSecret(), "/staff/" + userId + "/orgIds");
            conn.setRequestMethod("GET");

            HttpUtil.setBodyParameter("", conn);
            return (RceQueryResult) GsonUtil.fromJson(HttpUtil.returnResult(conn), RceQueryResult.class);
        } catch (Exception e) {
            log.info("failed to get staffIds from rce, error:{}", e.getMessage());
        }
        return null;
    }
}
