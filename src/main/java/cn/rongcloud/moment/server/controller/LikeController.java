package cn.rongcloud.moment.server.controller;

import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.utils.GsonUtil;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.pojos.Paged;
import cn.rongcloud.moment.server.pojos.ReqLikeIt;
import cn.rongcloud.moment.server.service.LikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author renchaoyang
 * @date 2021/6/9
 */
@Slf4j
@RestController
@RequestMapping("/like")
public class LikeController {

    @Resource
    private LikeService likeService;

    @PostMapping
    public RestResult likeIt(@RequestBody ReqLikeIt reqLike) {
        log.info("like it, operator:{}, data:{}", UserHolder.getUid(), GsonUtil.toJson(reqLike));
        return this.likeService.likeIt(reqLike);
    }

    @DeleteMapping("/{fid}")
    public RestResult unLikeIt(@PathVariable(name = "fid") String fid) {
        log.info("unlikeit, operator:{}, fid:{}", UserHolder.getUid(), fid);
        this.likeService.unLikeIt(fid);
        return RestResult.success();
    }


    @PostMapping("/{fid}")
    public RestResult getPagedLikes(@PathVariable(name = "fid") String fid, Paged page) {
        log.info("get paged likes, operator:{}, fid:{}, data:{}", UserHolder.getUid(), fid, GsonUtil.toJson(page));
        return this.likeService.getPagedLikes(fid, page);
    }

}
