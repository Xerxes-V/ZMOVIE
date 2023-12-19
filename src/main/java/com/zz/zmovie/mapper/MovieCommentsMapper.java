package com.zz.zmovie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zz.zmovie.po.MovieComments;
import com.zz.zmovie.vo.MyMovieComments;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MovieCommentsMapper extends BaseMapper<MovieComments> {

    //查找个人所有影评：
    @Select("select mc.id ,mc.content,mc.comment_time ,md.post,md.name,us.score ,mc.movie_id from movie_comments mc LEFT JOIN movie_detail md on mc.movie_id = md.id LEFT JOIN user_scored us on us.user_id = mc.user_id and us.movie_id = mc.movie_id WHERE mc.user_id = #{user_id};")
    List<MyMovieComments> getMyComments(Long user_id);

    /**
     * 查找近七天 数量变化
     */
    @Select("SELECT (CASE WHEN a2.num IS NOT NULL THEN a2.num ELSE 0 END)   AS num \n" +
            "from\n" +
            "(SELECT\n" +
            "date_format(@cdate := DATE_ADD(@cdate, INTERVAL - 1 DAY),'%m-%d') as days\n" +
            "FROM\n" +
            "(SELECT @cdate := DATE_ADD(NOW(), INTERVAL + 1 DAY) FROM `movie_comments`) t0\n" +
            "LIMIT 7) a1\n" +
            "left join\n" +
            "(SELECT DATE_FORMAT(`comment_time`, '%m-%d') AS days,COUNT(1) AS num FROM movie_comments GROUP BY DATE_FORMAT(`comment_time`, '%m-%d') ORDER BY DATE_FORMAT(`comment_time`, '%m-%d') DESC limit 7) a2\n" +
            " on a1.days = a2.days")
    List<Integer> getSevenDaysChangeNum();


    /**
     * 查找七天以前的数量
     */
    @Select("\n" +
            " SELECT  count(*) from movie_comments WHERE comment_time < DATE_SUB(CURDATE(), INTERVAL 7 DAY)")
    int getSevenDaysAgoNum();


    /**
     * 更新点赞数
     */

    @Update("UPDATE movie_comments set `likes` = `likes` + #{like} WHERE id = #{commentId} ")
    int updateLikes(Long commentId,int like);
}
