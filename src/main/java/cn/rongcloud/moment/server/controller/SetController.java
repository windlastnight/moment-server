package cn.rongcloud.moment.server.controller;

import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.utils.GsonUtil;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.pojos.ReqSetCover;
import cn.rongcloud.moment.server.service.CoverService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author renchaoyang
 * @date 2021/6/11
 */
@Slf4j
@RestController
@RequestMapping("/setting")
public class SetController {

    @Resource
    CoverService coverService;

    @PutMapping("/cover")
    public RestResult setCover(@Valid @RequestBody ReqSetCover setCover) {
        log.info("set cover, operator:{}, data:{}", UserHolder.getUid(), GsonUtil.toJson(setCover));
        this.coverService.setCover(setCover.getCover());
        return RestResult.success();
    }

    @GetMapping("/{uid}/cover")
    public RestResult getCover(@Param(value = "uid") String uid){
        return this.coverService.getCover(uid);
    }



}
