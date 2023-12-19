package com.zz.zmovie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zz.zmovie.po.MovieDetail;
import com.zz.zmovie.vo.MoviePage;
import com.zz.zmovie.vo.SearchMovie;
import com.zz.zmovie.vo.TopMovies;
import com.zz.zmovie.vo.admin.MovieManagement;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MovieDetailMapper extends BaseMapper<MovieDetail> {
    @Select("select  m.movie_id as 'id',md.name,md.release_date,md.genres,md.staring,md.post,round(m.score,1) as score from movie_detail md LEFT JOIN movie m on m.movie_id = md.id ORDER BY release_date desc limit #{start},18;")
    public List<TopMovies> getNewest(int start);

    @Select("select md.*,m.score,m.rating_num from movie_detail md LEFT JOIN movie m on m.movie_id = md.id   WHERE md.id = #{id} ;")
    public MoviePage getDetail(Long id);

    //查找最新36部
    @Select("select  m.movie_id as 'id',md.name,md.release_date,md.genres,md.staring,md.post,round(m.score,1) as score from movie_detail md LEFT JOIN movie m on m.movie_id = md.id ORDER BY release_date desc limit #{start},36;")
    public List<TopMovies> getNewest36(int start);

    //数量
    @Select("select count(*) from movie_detail")
    public int getMovieNums();

    //电影名相似
    @Select("select * from movie_detail md LEFT JOIN movie m on m.movie_id = md.id where name like #{name}")
    public List<SearchMovie> selectNameLike(String name);

    //查找电影名相同
    @Select("select md.id,md.name,md.release_date,md.genres,md.staring,md.post,m.score,m.collect_num,m.rating_num,m.comment_num from movie_detail md LEFT JOIN movie m on m.movie_id = md.id where name = #{name}")
    public List<SearchMovie> selectByName(String name);

    //查找内容相似：
    @Select("select md.id,md.name,md.release_date,md.genres,md.staring,md.post,m.score,m.collect_num,m.rating_num,m.comment_num from movie_detail md LEFT JOIN movie m on m.movie_id = md.id where md.summary like #{summary}")
    public List<SearchMovie> selectSummaryLike(String summary);

    /**
     * 根据传来的 id 顺序 批量查询
     */
    @Select({
            "<script>",
            "SELECT * FROM movie_detail md where md.id in",
            "<foreach collection='cameraIds' item='item' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "order by FIELD(id,",
            "<foreach collection='cameraIds' item='item' open=' ' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"
    })
     List<MovieDetail> getMovieDetailByIds(@Param("cameraIds") List<Long> item);

    /**
     * 查询不同国家地区的电影数量
     */
    @Select("SELECT count(1) from movie_detail where country in ('美国','中国大陆','中国香港','中国台湾')    GROUP BY country ORDER BY country;")
     List<Integer> getMovieNumsByArea();

    /**
     * 查找部分电影信息：
     */
    @Select("SELECT id,name,director,staring,country,release_date FROM movie_detail ORDER BY id DESC limit #{start},12;")
    List<MovieManagement> getMovieManagement(int start);


    /**
     *  根据电影名搜索出来给管理员管理
     */

    @Select("SELECT id,name,director,staring,country,release_date FROM movie_detail  WHERE `name` like  #{name}   ORDER BY id DESC limit #{start},12;")
    List<MovieManagement> searchMovieManagementByName(String name,int start);

    /**
     * 根据 类别 推荐电影
     */
    @Select("SELECT md.id as 'id',md.name,md.release_date,md.genres,md.staring,md.post,round(m.score,1) as score FROM movie_detail md INNER JOIN movie m on m.movie_id = md.id WHERE md.genres like  CONCAT('%',#{name},'%') limit 10;")
     List<TopMovies> getRecommendationByGenres(String genres);

    /**
     * 根据 传来的 id 返回 topMovies
     */
    @Select({
            "<script>",
            "SELECT md.id as 'id',md.name,md.release_date,md.genres,md.staring,md.post,round(m.score,1) as score FROM movie_detail md INNER JOIN movie m on m.movie_id = md.id WHERE md.id in",
            "<foreach collection='cameraIds' item='item' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"
    })
    List<TopMovies> getRecommendationByIds(@Param("cameraIds") List<Long> item);
}

