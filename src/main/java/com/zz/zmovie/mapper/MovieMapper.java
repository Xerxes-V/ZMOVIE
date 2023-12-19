package com.zz.zmovie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zz.zmovie.po.Movie;
import com.zz.zmovie.po.MovieDetail;
import com.zz.zmovie.vo.TopMovies;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MovieMapper extends BaseMapper<Movie> {

    /**
     *
     * @param type  1 最热 ，2 最多评论 ，3最多收藏
     * @return
     */
    @Select("SELECT m.movie_id as 'id',md.name,md.release_date,md.genres,md.staring,md.post,round(m.score,1) as score FROM top t LEFT JOIN movie m ON m.movie_id = t.movie_id LEFT JOIN movie_detail md on md.id=t.movie_id where type = ${type}  ORDER BY t.id;")
    List<TopMovies> getTopList(int type);


    @Select({"<script>",
            " SELECT ",
            "  m.movie_id as 'id',md.name,md.release_date,md.genres,md.staring,md.post,round(m.score,1) as score  FROM movie m LEFT JOIN movie_detail md on md.id=m.movie_id  ",
            "  WHERE md.id in ",
            "<foreach item='item' index='index' collection='items' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"})
    List<TopMovies> getFakeRe(@Param("items") List<Long> userIds);


    //修改评分
    @Update("UPDATE movie set rating_num = rating_num+1 ,score_sum = score_sum + #{score} WHERE movie_id = #{movie_id};")
    public int addScore(Long movie_id,int score);

    @Update("UPDATE movie set score_sum = score_sum + #{score} WHERE movie_id = #{movie_id};")
    public int updateScore(Long movie_id,int score);

    //修改评分数量：
    @Update("UPDATE movie set collect_num = collect_num + #{add} WHERE movie_id = #{movie_id};")
    public int collect(Long movie_id,int add);

    /**
     * 获取今日新增数量
     * @return
     */
    @Select(" select count(1) from movie  where to_days(put_on_date) =   to_days(now())")
    public int getTodayIncrease();

    /**
     * 根据评分 分类电影数量
     * @return
     */
    @Select("SELECT count(1) from movie WHERE FLOOR(score/2) > 0  GROUP BY  FLOOR(score/2);")
    public List<Integer> getNumsByScore();
}
