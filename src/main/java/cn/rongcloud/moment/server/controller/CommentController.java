package cn.rongcloud.moment.server.controller;

import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.utils.ApplicationUtil;
import cn.rongcloud.moment.server.common.utils.GsonUtil;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.pojos.Paged;
import cn.rongcloud.moment.server.model.Comment;
import cn.rongcloud.moment.server.pojos.ReqCreateComment;
import cn.rongcloud.moment.server.service.CommentService;
import com.google.common.collect.Lists;
import com.sun.webkit.dom.CommentImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @PostMapping("/expire")
    public RestResult getExpire(){
        RedisTemplate.class.cast(ApplicationUtil.getBean("redisTemplate")).expire("test", 1 , TimeUnit.MINUTES);
        return RestResult.success();
    }
    @PostMapping("/cache")
    public RestResult cache(){
        Comment c1 = new Comment();
        c1.setCommentId("001");
        Comment c2 = new Comment();
        c2.setCommentId("002");
        Comment c3 = new Comment();
        c3.setCommentId("003");
        Comment c4 = new Comment();
        c4.setCommentId("004");
        Set<DefaultTypedTuple> tuples = Lists.newArrayList(c1, c2, c3, c4).stream().map(cm -> new DefaultTypedTuple(cm, 0d)).collect(Collectors.toSet());
        RedisTemplate.class.cast(ApplicationUtil.getBean("redisTemplate")).opsForZSet().add("testtuples", tuples);
        return RestResult.success();
    }


}
