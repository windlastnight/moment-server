package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.im.IMHelper;
import cn.rongcloud.moment.server.common.rest.RestException;
import cn.rongcloud.moment.server.common.rest.RestResult;
import cn.rongcloud.moment.server.common.rest.RestResultCode;
import cn.rongcloud.moment.server.common.utils.IdentifierUtils;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.mapper.LikeMapper;
import cn.rongcloud.moment.server.model.Feed;
import cn.rongcloud.moment.server.model.Like;
import cn.rongcloud.moment.server.pojos.Paged;
import cn.rongcloud.moment.server.pojos.ReqLikeIt;
import cn.rongcloud.moment.server.pojos.RespLike;
import cn.rongcloud.moment.server.pojos.RespLikeIt;
import com.google.common.collect.Lists;
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

    @Override
    public RestResult likeIt(ReqLikeIt reqLike) throws RestException {
        String feedId = reqLike.getFeedId();
        Feed feed = this.feedService.checkFeedExists(feedId);
        checkUserLikeFeed(feedId);
        Like like = this.saveLike(feedId);
        RespLikeIt respLikeIt = new RespLikeIt();
        BeanUtils.copyProperties(like, respLikeIt);
        List receivers = Lists.newArrayList(feed.getUserId());
        this.imHelper.publishCommentNtf(receivers);

        return RestResult.success(respLikeIt);
    }


    private void checkUserLikeFeed(String feedId) throws RestException {
        Like like = getLikeByUser(feedId);
        if (Objects.nonNull(like)) {
            throw new RestException(RestResult.generic(RestResultCode.ERR_LIKE_USER_ALEADY_LIKED));
        }
    }

    private Like getLikeByUser(String feedId) {
        Like like = this.likeMapper.selectByFeedIdAndUserId(feedId, UserHolder.getUid());
        return like;
    }

    @Override
    public void unLikeIt(String feedId) throws RestException {
        this.feedService.checkFeedExists(feedId);
        Like like = this.getLikeByUser(feedId);
        if (Objects.isNull(like)){
            throw new RestException(RestResult.generic(RestResultCode.ERR_LIKE_USER_NO_LIKE));
        }
        this.likeMapper.deleteByPrimaryKey(like.getLikeId());
    }

    @Override
    public RestResult getPagedLikes(String fid, Paged page) throws RestException {

        this.feedService.checkFeedExists(fid);
        if (StringUtils.isNotBlank(page.getFromId())) {
            Like like = this.likeMapper.selectByPrimaryKey(page.getFromId());
            if (Objects.isNull(like)) {
                throw new RestException(RestResult.generic(RestResultCode.ERR_FEED_NOT_EXISTED));
            }
        }
        List<Like> likes = this.likeMapper.selectPagedComment(page);
        List<RespLike> res = likes.stream().map(cm -> {
            RespLike respLike = new RespLike();
            BeanUtils.copyProperties(cm, respLike);
            return respLike;
        }).collect(Collectors.toList());
        return RestResult.success(res);
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
