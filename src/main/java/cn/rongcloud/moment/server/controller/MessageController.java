package cn.rongcloud.moment.server.controller;

import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.utils.GsonUtil;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.pojos.ReqIds;
import cn.rongcloud.moment.server.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message")
@Slf4j
public class MessageController {

    @Autowired
    MessageService messageService;

    @GetMapping("/unread_count")
    public RestResult getUnreadCount() {
        log.info("get unread message count, operator:{}", UserHolder.getUid());
        return messageService.getUnreadCount();
    }

    @GetMapping("/unread")
    public RestResult getUnread() {
        log.info("get unread message, operator:{}", UserHolder.getUid());
        return messageService.getUnread();
    }

    @GetMapping("/history")
    public RestResult getHistory(@RequestParam(value = "from_message_id", required = false) String fromMessageId, @RequestParam(value = "size", defaultValue = "20") Integer size) {
        log.info("get history message, operator:{}, fromMessageId:{}, size:{}", UserHolder.getUid(), fromMessageId, size);
        return messageService.getHistory(fromMessageId, size);
    }

    @DeleteMapping("/batch")
    public RestResult batchDelete(@RequestBody ReqIds data) {
        log.info("batch delete message, operator:{}, data:{}", UserHolder.getUid(), GsonUtil.toJson(data));
        return messageService.batchDelete(data.getIds());
    }

    @DeleteMapping("/all")
    public RestResult deleteAll() {
        log.info("delete all message, operator:{}", UserHolder.getUid());
        return messageService.deleteAll();
    }

}
