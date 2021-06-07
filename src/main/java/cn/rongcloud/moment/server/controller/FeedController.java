package cn.rongcloud.moment.server.controller;

import cn.rongcloud.moment.server.common.jwt.JwtUser;
import cn.rongcloud.moment.server.common.jwt.filter.JwtFilter;
import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.utils.GsonUtil;
import cn.rongcloud.moment.server.pojos.RepIds;
import cn.rongcloud.moment.server.pojos.ReqFeedPublish;
import cn.rongcloud.moment.server.service.FeedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Created by sunyinglong on 2020/7/30
 */
@RestController
@RequestMapping("/feed")
@Slf4j
public class FeedController {

    @Autowired
    private FeedService feedService;

    @PostMapping
    public RestResult publish(@RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser authUser, @Valid @RequestBody ReqFeedPublish data) {
        log.info("publish feed, operator:{}, data:{}", authUser.getUserId(), GsonUtil.toJson(data));
        return feedService.publish(authUser.getUserId(), data);
    }

    @DeleteMapping("/{feedId}")
    public RestResult delete(@RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser authUser, @PathVariable String feedId) {
        log.info("delete feed, operator:{}, data:{}", authUser.getUserId(), feedId);
        return feedService.delete(authUser.getUserId(), feedId);
    }

    @GetMapping("/{feedId}")
    public RestResult getFeedInfo(@RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser authUser, @PathVariable String feedId) {
        log.info("get feed, operator:{}, data:{}", authUser.getUserId(), feedId);
        return feedService.getFeedInfo(authUser.getUserId(), feedId);
    }

    @PostMapping("/batch")
    public RestResult batchGetFeedInfo(@RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser authUser, @RequestBody RepIds data) {
        log.info("batch get feed, operator:{}, data:{}", authUser.getUserId(), GsonUtil.toJson(data));
        return feedService.batchGetFeedInfo(authUser.getUserId(), data.getIds());
    }
}
