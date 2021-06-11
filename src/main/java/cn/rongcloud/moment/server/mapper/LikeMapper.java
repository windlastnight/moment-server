package cn.rongcloud.moment.server.mapper;


import cn.rongcloud.moment.server.model.Like;
import cn.rongcloud.moment.server.pojos.Paged;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LikeMapper {
    int deleteByPrimaryKey(String likeId);

    int insert(Like record);

    int insertSelective(Like record);

    Like selectByPrimaryKey(String likeId);

    int updateByPrimaryKeySelective(Like record);

    int updateByPrimaryKey(Like record);

    Like selectByFeedIdAndUserId(@Param("feedId") String feedId, @Param("userId") String uid);

    List<Like> selectPagedComment(Paged page);
}