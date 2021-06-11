package cn.rongcloud.moment.server.mapper;


import cn.rongcloud.moment.server.model.Comment;
import cn.rongcloud.moment.server.pojos.Paged;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {
    int deleteByPrimaryKey(String commentId);

    int insert(Comment record);

    int insertSelective(Comment record);

    Comment selectByPrimaryKey(String commentId);

    int updateByPrimaryKeySelective(Comment record);

    int updateByPrimaryKey(Comment record);

    Comment selectLastUserCommet(@Param("feedId") String feedId, @Param("userId") String uid);

    List<Comment> selectPagedComment(Paged page);

    List<String> getAllCommentAndLikeUserIds(String feedId);
}