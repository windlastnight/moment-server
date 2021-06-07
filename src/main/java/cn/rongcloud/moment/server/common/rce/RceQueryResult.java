package cn.rongcloud.moment.server.common.rce;

import lombok.Data;
import java.util.List;

@Data
public class RceQueryResult {

    // 返回码
    Integer code;
    // result
    List<String> result;

    public RceQueryResult(Integer code, List<String> result) {
        this.code = code;
        this.result = result;
    }

    public boolean isSuccess() {
        return code == 10000;
    }

}
