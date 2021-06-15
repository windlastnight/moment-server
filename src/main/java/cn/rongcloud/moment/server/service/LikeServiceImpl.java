package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.im.IMHelper;
import cn.rongcloud.moment.server.common.redis.RedisKey;
import cn.rongcloud.moment.server.common.redis.RedisOptService;
import cn.rongcloud.moment.server.common.rest.RestException;
import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.rest.RestResultCode;
import cn.rongcloud.moment.server.common.utils.DateTimeUtils;
import cn.rongcloud.moment.server.common.utils.IdentifierUtils;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.enums.MessageStatus;
import cn.rongcloud.moment.server.enums.MomentsCommentType;
import cn.rongcloud.moment.server.mapper.LikeMapper;
import cn.rongcloud.moment.server.model.Feed;
import cn.rongcloud.moment.server.model.Like;
import cn.rongcloud.moment.server.model.LikeNotifyData;
import cn.rongcloud.moment.server.model.Message;
import cn.rongcloud.moment.server.pojos.Paged;
import cn.rongcloud.moment.server.pojos.ReqLikeIt;
import cn.rongcloud.moment.server.pojos.RespLike;
import cn.rongcloud.moment.server.pojos.RespLikeIt;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author renchaoyang
 * @date 2021/6/9
 */
@Transactional
@Service
public class LikeServiceImpl implements LikeService {

    @Resource
    FeedService feedService;

    @Resource
    LikeMapper likeMapper;

    @Resource
    IMHelper imHelper;

    @Resource
    CommentService commentService;

    @Resource
    MessageService messageService;

    @Resource
    RedisOptService redisOptService;

    @Override
    public RestResult likeIt(ReqLikeIt reqLike) throws RestException {
        String feedId = reqLike.getFeedId();
        Feed feed = this.feedService.checkFeedExists(feedId);
        checkUserLikeFeed(feedId);
        Like like = this.saveLike(feedId);
        RespLikeIt respLikeIt = new RespLikeIt();
        BeanUtils.copyProperties(like, respLikeIt);
        List<String> receivers = this.commentService.getCommentNtfReceivers(feed);

        LikeNotifyData likeNotifyData = new LikeNotifyData();
        BeanUtils.copyProperties(like, likeNotifyData);
        likeNotifyData.setCreateDt(like.getCreateDt().getTime());
        this.imHelper.publishCommentNtf(receivers, likeNotifyData, MomentsCommentType.LIKE);

        Message message = new Message();
        message.setMessageId(like.getLikeId());
        message.setUserId(UserHolder.getUid());
        message.setCreateDt(DateTimeUtils.currentDt());
        message.setMessageType(MomentsCommentType.LIKE.getType());
        message.setStatus(MessageStatus.NORMAL.getValue());
        messageService.saveMessage(message);

        if (receivers != null && !receivers.isEmpty()) {
            for (String receiverId: receivers) {
                if (receiverId.equals(UserHolder.getUid())) {
                    continue;
                }
                redisOptService.zsAdd(RedisKey.getUserUnreadMessageKey(receiverId), message, DateTimeUtils.currentDt().getTime());
            }
        }

        return RestResult.success(respLikeIt);
    }


    private void checkUserLikeFeed(String feedId) throws RestException {
        Like like = getLikeByUser(feedId);
        if (Objects.nonNull(like)) {
            throw new RestException(RestResult.generic(RestResultCode.ERR_LIKE_USER_ALEADY_LIKED));
        }
    }

    private Like getLikeByUser(String feedId) {
        return this.likeMapper.selectByFeedIdAndUserId(feedId, UserHolder.getUid());
    }

    @Override
    public void unLikeIt(String feedId) throws RestException {
        this.feedService.checkFeedExists(feedId);
        Like like = this.getLikeByUser(feedId);
        if (Objects.isNull(like)) {
            throw new RestException(RestResult.generic(RestResultCode.ERR_LIKE_USER_NO_LIKE));
        }
        this.likeMapper.deleteByPrimaryKey(like.getId());
    }

    @Override
    public RestResult getPagedLikes(String fid, Paged page) throws RestException {

        this.feedService.checkFeedExists(fid);

        List<Like> likes = getLikes(fid, page.getFromUId(), page.getSize());

        List<RespLike> res = likes.stream().map(cm -> {
            RespLike respLike = new RespLike();
            BeanUtils.copyProperties(cm, respLike);
            return respLike;
        }).collect(Collectors.toList());
        return RestResult.success(res);
    }

    @Override
    public List<Like> getLikes(String feedId, String fromLikeId, int size) {
        Long fromAutoIncLikeId = null;
        if (StringUtils.isNotBlank(fromLikeId)) {
            Like like = likeMapper.selectByLikeId(fromLikeId);
            if (Objects.isNull(like)) {
                throw new RestException(RestResult.generic(RestResultCode.ERR_LIKE_USER_NO_LIKE));
            }
            fromAutoIncLikeId = like.getId();
        }
        return this.likeMapper.selectPagedLike(feedId, fromAutoIncLikeId, size);
    }

    private Like saveLike(String feedId) {
        Like savedLike = new Like();
        savedLike.setCreateDt(new Date());
        savedLike.setFeedId(feedId);
        savedLike.setUserId(UserHolder.getUid());
        savedLike.setLikeId(IdentifierUtils.uuid24());
        this.likeMapper.insertSelective(savedLike);
        return savedLike;
    }

}
