package com.zz.zmovie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zz.zmovie.service.MovieService;
import com.zz.zmovie.service.UserService;
import com.zz.zmovie.config.UserThreadLocal;
import com.zz.zmovie.mapper.*;
import com.zz.zmovie.po.*;
import com.zz.zmovie.redis.MoviePrefix;
import com.zz.zmovie.redis.RedisPrefix;
import com.zz.zmovie.redis.RedisService;
import com.zz.zmovie.redis.UserPrefix;
import com.zz.zmovie.vo.SearchMovie;
import com.zz.zmovie.vo.TopMovies;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.JDBCDataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MovieServiceImpl implements MovieService {

    @Autowired
    private TopMapper topMapper;

    @Autowired
    private MovieService movieService;

    @Autowired
    private MovieMapper movieMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserService userService;

    @Autowired
    private CollectionsMapper collectionsMapper;

    @Autowired
    private MovieDetailMapper movieDetailMapper;

    @Autowired
    private UserScoredMapper userScoredMapper;

    @Autowired
    private RecommendationMapper recommendationMapper;

    /**
     * 评分最高的电影
     *
     * @return
     */
    @Override
    public List<TopMovies> getHottest() {
        List<TopMovies> list = getTops(MoviePrefix.topPrefix, "hottest", TopMovies.class);

        if (list == null) {
            System.out.println("hottest null");
            list = movieMapper.getTopList(1);        //数据库查找并且存回 redis
            redisService.setList(MoviePrefix.topPrefix, "hottest", list);
        }
//        list = judgeIsCollected(list);      //判断是否收藏

        return list;
    }

    /**
     * 最新电影
     *
     * @param start 开始的序号
     * @return
     */
    @Override
    public List<TopMovies> getNewest(int start) {

        List<TopMovies> list = null;
        if (start == 0) {
            list = getTops(MoviePrefix.topPrefix, "newest", TopMovies.class);
            if (list == null) {
                list = movieDetailMapper.getNewest(start);
                redisService.setList(MoviePrefix.topPrefix, "newest", list);      //redis 只存储一页
            }
        } else {
            list = movieDetailMapper.getNewest(start * 18);
        }

//        list = judgeIsCollected(list);      //判断是否收藏

        return list;
    }

    /**
     * 查找最多评论的
     *
     * @return
     */
    @Override
    public List<TopMovies> getMostComment() {
        //先后：
        List<TopMovies> mostComment = getTops(MoviePrefix.topPrefix, "mostComment", TopMovies.class);

        if (mostComment == null) {
            mostComment = movieMapper.getTopList(2);
            redisService.set(MoviePrefix.topPrefix, "mostComment", mostComment);
        }

        return mostComment;
    }


    /**
     * 最多收藏
     *
     * @return
     */
    @Override
    public List<TopMovies> getMostCollected() {
        List<TopMovies> mostCollected = getTops(MoviePrefix.topPrefix, "mostCollected", TopMovies.class);

        if (mostCollected == null) {
            mostCollected = movieMapper.getTopList(3);
            redisService.set(MoviePrefix.topPrefix, "mostCollected", mostCollected);
        }

        return mostCollected;
    }

    /**
     * 推荐
     *
     * @return
     */
    public List<TopMovies> getRecommendation() {
        User user = UserThreadLocal.get();

        QueryWrapper<Recommendation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",user.getId());
        List<Recommendation> list = recommendationMapper.selectList(queryWrapper);

        if(list == null || list.size() == 0){       //初始化需要加载
            List<Long> ids  =  getRecommendationByGenres(user.getId(),user.getLabel());
            List<TopMovies> res = movieDetailMapper.getRecommendationByIds(ids);
            return res;
        }

        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            ids.add(list.get(i).getMovieId());
        }

        List<TopMovies> res = movieDetailMapper.getRecommendationByIds(ids);

        return res;
    }

    /**
     * 最新36部，返回页码和最大页码
     *
     * @return
     */
    @Override
    public Map<String, Object> getNew36(int curPage) {
        List<TopMovies> list = null;
        if (curPage == 1) {
            list = getTops(MoviePrefix.topPrefix, "newest36", TopMovies.class);
            if (list == null) {
                list = movieDetailMapper.getNewest36(curPage);
                redisService.setList(MoviePrefix.topPrefix, "newest36", list);      //redis 只存储一页
            }
        } else {
            list = movieDetailMapper.getNewest36((curPage - 1) * 36);
        }


        int movieNum = 0;
        if (redisService.existKey(MoviePrefix.MovieData, "movieNum")) {
            movieNum = redisService.get(MoviePrefix.MovieData, "movieNum", Integer.class);
        } else {
            movieNum = movieDetailMapper.getMovieNums();
            redisService.set(MoviePrefix.MovieData, "movieNum", movieNum);
        }

        Map<String, Object> map = new HashMap();
        map.put("movies", list);
        map.put("num", movieNum);

        return map;
    }


    /**
     * 根据筛选信息筛选电影
     *
     * @param map
     * @return
     */
    @Override
    public Page<MovieDetail> screen(Map<String, Object> map) {
        QueryWrapper<MovieDetail> queryWrapper = new QueryWrapper<>();

        String genre = map.get("genre").equals("全部类型") ? "" : map.get("genre").toString();

        String area = map.get("area").toString();

        int time = map.get("time").toString().equals("") ? 0 : Integer.parseInt(map.get("time").toString());
        String language = map.get("language").toString();


        queryWrapper.like("genres", genre);

        if (!area.equals("") && !area.equals("全部地区")) {
            queryWrapper.and(c -> c.eq("country", area));
        }

        if (!language.equals("") && !language.equals("不限")) {
            queryWrapper.and(c -> c.eq("language", language));
        }

        if (time == 1969) {
            queryWrapper.and(c -> c.le("release_date", time));
        } else if (time != 0) {
            queryWrapper.and(c -> c.lt("release_date", time + 10))
                    .and(c -> c.ge("release_date", time));
        }

//
        Page<MovieDetail> page = new Page<>(Integer.parseInt(map.get("curPage").toString()), 36);
        Page<MovieDetail> res = movieDetailMapper.selectPage(page, queryWrapper);
        System.out.println(res);

        return res;
    }

    /**
     * 电影搜索，根据电影名、英文名、相似like 查询。
     * 并且，依照查找相似性进行排序：
     *
     * @param name
     * @return
     */
    @Override
    public Set<SearchMovie> searchMovie(String msg) {

//        //1.首先名字相同：
//        QueryWrapper<MovieDetail> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("name", msg);
//        List<MovieDetail> movieDetail = movieDetailMapper.selectList(queryWrapper);
//
//        //2.英文名相同：
//        queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("english_name", msg);
//        movieDetail.addAll( movieDetailMapper.selectList(queryWrapper));
//
//        //3.电影名 like：
//        queryWrapper = new QueryWrapper<>();
//        queryWrapper.like("name",msg);
//        if(msg.length() > 3){
//            queryWrapper.like("summary",msg);
//        }
//        movieDetail.addAll( movieDetailMapper.selectList(queryWrapper));
//
//        Set<MovieDetail> set = new HashSet<>(movieDetail);

        //1.首先名字相同：
        List<SearchMovie> movieDetail = movieDetailMapper.selectByName(msg);
        //2.英文名相同：
        movieDetail.addAll( movieDetailMapper.selectNameLike("%"+msg+"%"));
        //3.电影名 like：
        if(msg.length() > 3){
            movieDetail.addAll( movieDetailMapper.selectSummaryLike("%"+msg+"%"));
        }
        Set<SearchMovie> res = new HashSet<>(movieDetail);

        return res;
    }

    /**
     * 展示用户的收藏列表：
     * @return
     */
    @Override
    public List<MovieDetail> viewCollections() {
        User user = UserThreadLocal.get();
        List<Long> ids = redisService.getListRedis(UserPrefix.userCollections,String.valueOf(user.getId()),Long.class);
        List<MovieDetail> res  = movieDetailMapper.getMovieDetailByIds(ids);
        return res;
    }

    /**
     * 展示历史记录
     * @return
     */
    @Override
    public List<MovieDetail> viewHistory() {
        User user = UserThreadLocal.get();
        List<Long> ids = redisService.getListRedis(UserPrefix.userHistory,String.valueOf(user.getId()),Long.class);


        for (Long id : ids) {
            System.out.println(id);
            
        }
        
        List<MovieDetail> res  = movieDetailMapper.getMovieDetailByIds(ids);

        return res;
    }

    //    --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------








    /**
     * 从redis中查找排行榜
     *
     * @param redisPrefix
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    //从 redis  top：
    public <T> List<T> getTops(RedisPrefix redisPrefix, String key, Class<T> clazz) {

        //先从 redis 找：
        List<T> list = redisService.getList(redisPrefix, key, clazz);
        if (list != null) {
            return list;
        }


        return null;
    }



    //定时维护：
    public void maintenanceHottest() {
        QueryWrapper<Movie> queryWrapper = new QueryWrapper();      //查找电影数据
        queryWrapper.orderByDesc("rating_num")
                .orderByDesc("score")
                .select("movie_id");

        Page<Movie> page = new Page<>(1, 10);
        Page<Movie> resPage = movieMapper.selectPage(page, queryWrapper);
        System.out.println(resPage);
    }

    public List<Long> getRecommendationByGenres(Long userId,String label){
        System.out.println("label:" +label);
        String genres[] = label.split(" ");

        List<TopMovies> movies = new ArrayList<>();

        List<Long> res = new ArrayList<>();

        for(String s : genres){
            List<TopMovies> similar = movieDetailMapper.getRecommendationByGenres(s);
            for (int i = 0; i < similar.size(); i++) {
                movies.add(similar.get(i));
            }
        }

        Random r = new Random();
        int other = -1;
        Set<Integer> indexs = new HashSet<>();
        while(indexs.size() < 10){
            indexs.add(r.nextInt(movies.size()));
            if(indexs.size() >= movies.size()){
                indexs.add(other--);
            }
        }

        int rank = 0;
        for (int i : indexs) {
            Recommendation recommendation = new Recommendation();
            recommendation.setUserId(userId);
            if(i < 0){
                Long movieId = Long.parseLong(String.valueOf(r.nextInt(4481)));
                recommendation.setMovieId(movieId);
                res.add(movieId);
            }else {
                recommendation.setMovieId(movies.get(i).getId());
                res.add(movies.get(i).getId());
            }
            recommendation.setRank(rank++);
            recommendationMapper.insert(recommendation);
        }

        return res;
    }


}
