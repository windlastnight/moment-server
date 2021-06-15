package cn.rongcloud.moment.server.controller;

import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.utils.UserHolder;
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

}
