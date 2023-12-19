package com.zz.zmovie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zz.zmovie.po.UserScored;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserScoredMapper extends BaseMapper<UserScored> {

    /**
     * 如果数据库里已经有评分数据那就更改分数，否则新增评分
     * @param movie_id
     * @param user_id
     * @param score
     * @return
     */
    @Insert("INSERT INTO user_scored (user_id,movie_id,score,score_time) VALUES (#{user_id},#{movie_id},#{score},NOW()) ON DUPLICATE KEY UPDATE score  = VALUES(score);")
    public int updateScore(Long movie_id,Long user_id,int score);


    /**
     * 查找今日评分次数最多的电影：
     */

    @Select("  select  md.name from user_scored us LEFT JOIN movie_detail md on md.id = us.movie_id where to_days(score_time) =  to_days(now()) GROUP  BY movie_id ORDER BY count(1) desc limit 0,1;")
    String getHottest();

    /**
     * 查找评分次数大于 5 的用户
     */
    @Select("SELECT user_id from user_scored WHERE user_id > 224 GROUP BY user_id having count(*) > 5 ORDER BY user_id desc;")
    List<Long> getMuchRatingsUser();
}
