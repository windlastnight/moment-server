package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.mapper.UserSettingMapper;
import cn.rongcloud.moment.server.model.UserSetting;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * @author renchaoyang
 * @date 2021/6/11
 */
@Service
public class CoverServiceImpl implements CoverService{

    @Resource
    UserSettingMapper userSettingMapper;

    @Override
    public void setCover(String cover) {
        UserSetting userSetting = this.userSettingMapper.selectByPrimaryKey(UserHolder.getUid());
        UserSetting toUpdateSetting = new UserSetting();
        toUpdateSetting.setCover(cover);
        toUpdateSetting.setUserId(UserHolder.getUid());
        toUpdateSetting.setUpdateDt(new Date());
        if(Objects.isNull(userSetting)){
            this.userSettingMapper.insertSelective(toUpdateSetting);
        }else{
            this.userSettingMapper.updateByPrimaryKeySelective(toUpdateSetting);
        }
    }

    @Override
    public RestResult getCover(String uid) {
        return RestResult.success(Optional.ofNullable(this.userSettingMapper.selectByPrimaryKey(uid)).map(UserSetting::getCover).orElse(""));
    }
}
