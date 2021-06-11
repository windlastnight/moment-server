package cn.rongcloud.moment.server.controller;

import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.utils.GsonUtil;
import cn.rongcloud.moment.server.common.utils.UserHolder;
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
    FeedService feedService;

    @PostMapping
    public RestResult publish(@Valid @RequestBody ReqFeedPublish data) {
        log.info("publish feed, operator:{}, data:{}", UserHolder.getUid(), GsonUtil.toJson(data));
        return feedService.publish(UserHolder.getUid(), data);
    }

    @DeleteMapping("/{feedId}")
    public RestResult delete(@PathVariable String feedId) {
        log.info("delete feed, operator:{}, data:{}", UserHolder.getUid(), feedId);
        return feedService.delete(UserHolder.getUid(), feedId);
    }

    @GetMapping("/{feedId}")
    public RestResult getFeedInfo(@PathVariable String feedId) {
        log.info("get feed, operator:{}, data:{}", UserHolder.getUid(), feedId);
        return feedService.getFeedInfo(UserHolder.getUid(), feedId);
    }

    @PostMapping("/batch")
    public RestResult batchGetFeedInfo(@RequestBody RepIds data) {
        log.info("batch get feed, operator:{}, data:{}", UserHolder.getUid(), GsonUtil.toJson(data));
        return feedService.batchGetFeedInfo(UserHolder.getUid(), data.getIds());
    }

    @GetMapping("/new")
    public RestResult getNewFeed(@RequestParam("latest_feed_id") String latestFeedId) {
        log.info("get new feed, operator:{}, data:{}", UserHolder.getUid(), latestFeedId);
        return feedService.getNewFeed(latestFeedId);
    }
}
