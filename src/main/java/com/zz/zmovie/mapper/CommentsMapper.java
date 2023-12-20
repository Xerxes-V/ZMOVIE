package com.zz.zmovie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zz.zmovie.po.Comments;
import com.zz.zmovie.vo.AllComments;
import com.zz.zmovie.vo.MyComments;
import com.zz.zmovie.vo.SonsComments;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CommentsMapper extends BaseMapper<Comments> {

    @Select("select mc.*,us.score, u.username,u.avatar from movie_comments mc LEFT JOIN user_scored us ON us.user_id = mc.user_id  AND us.movie_id = mc.movie_id LEFT JOIN `user` u on u.id = mc.user_id  where mc.movie_id = #{movie_id} limit #{start},30;")
    List<AllComments> getAllMovieComments(Long movie_id, int start);

    @Select("SELECT c.*,u.username,u.avatar from comments c LEFT JOIN movie_comments mc ON mc.id = c.mc_id LEFT JOIN `user` u on u.id = c.user_id  where c.mc_id = #{movieCommentId};")
    List<SonsComments> getSons(Long movieCommentId);

    /**
     * 查找个人所有回复消息
     * @return
     */
    @Select("SELECT c.id,c.comment_time,c.content,c.movie_id,md.name from comments c LEFT JOIN movie_detail md on md.id = c.movie_id WHERE  user_id = #{user_id}")
    List<MyComments> getPersonalComments(Long user_id);


    /**
     * 获取近七天每天的评论新增的数量
     * @return
     */
    @Select("SELECT  (CASE WHEN a2.num IS NOT NULL THEN a2.num ELSE 0 END) AS num \n" +
            "from\n" +
            "(SELECT\n" +
            "date_format(@cdate := DATE_ADD(@cdate, INTERVAL - 1 DAY),'%m-%d') as days\n" +
            "FROM\n" +
            "(SELECT @cdate := DATE_ADD(NOW(), INTERVAL + 1 DAY) FROM `comments`) t0\n" +
            "LIMIT 7) a1\n" +
            "left join\n" +
            "(SELECT DATE_FORMAT(`comment_time`, '%m-%d') AS days,COUNT(1) AS num FROM comments GROUP BY DATE_FORMAT(`comment_time`, '%m-%d') ORDER BY DATE_FORMAT(`comment_time`, '%m-%d') DESC limit 7) a2\n" +
            " on a1.days = a2.days")
    List<Integer> getEverydayNums();

    /**
     * 获取七天之前的数据
     * @return
     */
    @Select("\n" +
            " SELECT  count(*) from comments WHERE comment_time < DATE_SUB(CURDATE(), INTERVAL 7 DAY)")
    int getSevenDaysAgoNums();


    /**
     * 更新点赞数
     */

    @Update("UPDATE comments set `likes` = `likes` + #{like} WHERE id = #{commentId} ")
    int updateLikes(Long commentId, int like);

}
