package cn.rongcloud.moment.server.mapper;

import cn.rongcloud.moment.server.model.Like;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LikeMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Like record);

    int insertSelective(Like record);

    Like selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Like record);

    int updateByPrimaryKey(Like record);

    Like selectByFeedIdAndUserId(@Param("feedId") String feedId, @Param("userId") String uid);

    List<Like> selectPagedLike(@Param("feedId") String feedId, @Param("fromLikeId") Long page, @Param("size") Integer size);

    Like selectByLikeId(String likeId);

    List<Like> batchGetLikes(@Param("likeIds") List<String> likeIds);
}