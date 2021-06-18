package cn.rongcloud.moment.server.controller;

import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.utils.GsonUtil;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.pojos.ReqIds;
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
    public RestResult getFeedInfo(@PathVariable String feedId,
                                  @RequestParam(value = "with_comments", required = false, defaultValue = "true") boolean withComments,
                                  @RequestParam(value = "comment_size", required = false, defaultValue = "20") int commentSize,
                                  @RequestParam(value = "with_likes", required = false, defaultValue = "true") boolean withLikes,
                                  @RequestParam(value = "like_size", required = false, defaultValue = "20") int likeSize) {
        log.info("get feed, operator:{}, feedId:{}, with_comments:{}, commentSize:{}, withLikes:{}, likeSize:{}", UserHolder.getUid(), feedId, withComments, commentSize, withLikes, likeSize);
        return feedService.getFeedInfo(feedId, withComments, commentSize, withLikes, likeSize);
    }

    @PostMapping("/batch")
    public RestResult batchGetFeedInfo(@RequestBody ReqIds data,
                                       @RequestParam(value = "with_comments", required = false, defaultValue = "true") boolean withComments,
                                       @RequestParam(value = "comment_size", required = false, defaultValue = "20") int commentSize,
                                       @RequestParam(value = "with_likes", required = false, defaultValue = "true") boolean withLikes,
                                       @RequestParam(value = "like_size", required = false, defaultValue = "20") int likeSize) {
        log.info("batch get feed, operator:{}, feedIds:{}, with_comments:{}, commentSize:{}, withLikes:{}, likeSize:{}", UserHolder.getUid(), GsonUtil.toJson(data.getIds()), withComments, commentSize, withLikes, likeSize);
        return feedService.batchGetFeedInfo(data.getIds(), withComments, commentSize, withLikes, likeSize);
    }

    @GetMapping("/new")
    public RestResult getNewFeed(@RequestParam("latest_feed_id") String latestFeedId) {
        log.info("get new feed, operator:{}, data:{}", UserHolder.getUid(), latestFeedId);
        return feedService.getNewFeed(latestFeedId);
    }
}
