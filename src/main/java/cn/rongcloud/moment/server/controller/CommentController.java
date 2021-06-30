package cn.rongcloud.moment.server.controller;

import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.utils.GsonUtil;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.pojos.Paged;
import cn.rongcloud.moment.server.pojos.ReqCreateComment;
import cn.rongcloud.moment.server.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author renchaoyang
 * @date 2021/6/9
 */
@Slf4j
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @PostMapping
    public RestResult comment(@Valid @RequestBody ReqCreateComment comment) {
        log.info("comment, operator:{}, data:{}", UserHolder.getUid(), GsonUtil.toJson(comment));
        return this.commentService.comment(comment);
    }

    @DeleteMapping("/{fid}/{cid}")
    public RestResult delComment(@PathVariable(name = "fid") String fid,
                                 @PathVariable(name = "cid")String cid){
        log.info("delete comment, operator:{}, data:{}", UserHolder.getUid(), fid + "/" + cid);
        this.commentService.delComment(fid, cid);
        return RestResult.success();
    }

    @PostMapping("/{fid}")
    public RestResult getPagedComments(@PathVariable(name = "fid") String fid, Paged page){
        log.info("get paged comments, operator:{}, fid:{}, data:{}", UserHolder.getUid(), fid, GsonUtil.toJson(page));
        return this.commentService.getPagedComments(fid, page);
    }

}
