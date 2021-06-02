package cn.rongcloud.moment.server.common.im.pojos;

import lombok.Data;

import java.util.List;

/**
 * Created by sunyinglong on 2020/6/25
 */
@Data
public class IMGagChatRoomUserResult {
    // 返回码，200 为正常.
    Integer code;

    // 被禁言用户数组
    List<IMGagRoomUserInfo> users;

    // 解封时间
    String time;

    //被禁言用户 Id
    String userId;

    public boolean isSuccess() {
        return code == 200;
    }

}
