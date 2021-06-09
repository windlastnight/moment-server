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
    public RestResult getTimeLine(@RequestParam("from_feed_id") String fromFeedId, @RequestParam("size") Integer size) {
        log.info("get time line,operator:{}, from_feed_id:{}, size:{}", UserHolder.getUid(), fromFeedId, size);
        return timelineService.getTimeLine(fromFeedId, size);
    }

}
