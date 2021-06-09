package cn.rongcloud.moment.server.controller;

import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.service.TimelineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by sunyinglong on 2020/7/30
 */
@RestController
@RequestMapping("/timeline")
@Slf4j
public class TimelineController {

    @Autowired
    private TimelineService timelineService;

    @GetMapping
    public RestResult getMineTimeLine(@RequestParam(value = "from_feed_id", required = false) String fromFeedId, @RequestParam("size") Integer size) {
        log.info("get mine time line,operator:{}, from_feed_id:{}, size:{}", UserHolder.getUid(), fromFeedId, size);
        return timelineService.getMineTimeLine(fromFeedId, size);
    }

    @GetMapping("/{uid}")
    public RestResult getUserTimeLine(@RequestParam(value = "from_feed_id", required = false) String fromFeedId, @RequestParam("size") Integer size, @PathVariable("uid") String userId) {
        log.info("get user time line,operator:{}, from_feed_id:{}, size:{}, userId:{}", UserHolder.getUid(), fromFeedId, size, userId);
        return timelineService.getUserTimeLine(fromFeedId, size, userId);
    }

}
