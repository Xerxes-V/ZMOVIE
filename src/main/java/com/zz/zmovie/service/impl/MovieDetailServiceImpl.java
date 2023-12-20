package com.zz.zmovie.service.impl;

import cn.hutool.json.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zz.zmovie.rabbitMq.CollectionsAssist;
import com.zz.zmovie.rabbitMq.MqService;
import com.zz.zmovie.service.MovieDetailService;
import com.zz.zmovie.config.UserThreadLocal;
import com.zz.zmovie.mapper.CollectionsMapper;
import com.zz.zmovie.mapper.MovieDetailMapper;
import com.zz.zmovie.mapper.MovieMapper;
import com.zz.zmovie.po.*;
import com.zz.zmovie.redis.MoviePrefix;
import com.zz.zmovie.redis.RedisService;
import com.zz.zmovie.redis.UserPrefix;
import com.zz.zmovie.utils.QiniuService;
import com.zz.zmovie.vo.MoviePage;
import com.zz.zmovie.vo.admin.UploadMovie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class MovieDetailServiceImpl implements MovieDetailService {
    @Autowired
    private CollectionsMapper collectionsMapper;

    @Autowired
    private MovieDetailMapper movieDetailMapper;

    @Autowired
    private MovieMapper movieMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MqService mqService;

    @Autowired
    private QiniuService qiniuService;


    //mq 队列：
    private static final String userScoreQueue = "userScore";
    private static final String movieQueue = "movie";
    private static final String collectionsQueue = "collections";
    private static final String movieDQueue = "movieDetail";



    /**
     * 电影详情所需要的数据
     *
     * @param movie_id
     * @return
     */
    @Override
    public MoviePage getMovieDetail(Long movie_id) {

        User user = UserThreadLocal.get();
        //先 redis：
        MoviePage moviePage = redisService.get(MoviePrefix.getMovieDetail, String.valueOf(movie_id), MoviePage.class);

        if (moviePage != null) {
            redisService.setListRedis(UserPrefix.userHistory,String.valueOf(user.getId()),movie_id);
            moviePage.setCollected(judgeIsCollected(user.getId(),movie_id));            //由于 改变收藏的时候只会改变用户收藏列表，而不会改变 redis 的 moviePage，因此要判断
            moviePage.setMyScore(getMyScore(user.getId(),movie_id));
            moviePage.setUserId(user.getId());
            return moviePage;
        }



        moviePage = movieDetailMapper.getDetail(movie_id);
        redisService.set(MoviePrefix.getMovieDetail, String.valueOf(movie_id), moviePage);
        moviePage.setCollected(judgeIsCollected(user.getId(),movie_id));
        moviePage.setMyScore(getMyScore(user.getId(),movie_id));

        //加入历史访问记录：
        redisService.setListRedis(UserPrefix.userHistory,String.valueOf(user.getId()),movie_id);

        moviePage.setUserId(user.getId());

        return moviePage;
    }

    /**
     * 收藏 or not ，需要配合 MQ 使用
     * @param isCollected
     * @param movie_id
     * @param user_id
     * @return
     */
    @Override
    public  int doCollect(boolean isCollected,Long movie_id)  {
        User user = UserThreadLocal.get();

        //先 redis 更改

            if(isCollected){
                redisService.setListRedis(UserPrefix.userCollections,String.valueOf(user.getId()),movie_id);
            }else{
                redisService.delList(UserPrefix.userCollections,String.valueOf(user.getId()),movie_id);
            }


            //新建收藏信息
            CollectionsAssist collectionsAssist = new CollectionsAssist();
            collectionsAssist.setCollected(isCollected);
            collectionsAssist.setMovieId(movie_id);
            collectionsAssist.setUserId(user.getId());

            String updateRequest = RedisService.beanToString(collectionsAssist);

           return  mqService.updateMsg(collectionsQueue,updateRequest);

    }


    /**
     * 新增 or 改变评分，oldScore 为以前评分，若 oldScore 为0 ，则未评分过 新增，反之修改
     * @param movie_id
     * @param score
     * @param oldScore
     * @return
     */
    @Override
    public int updateScore(Long movie_id, int score,int oldScore) {
        User user = UserThreadLocal.get();

        UserScored userScored = new UserScored();
        userScored.setMovieId(movie_id);
        userScored.setScore(score);
        userScored.setScoreTime(new Date());

        String newScore  = RedisService.beanToString(userScored);

        if(oldScore > 0){                       //修改分数
            //构造老的评分数据 用于 redis 删除
            userScored.setScore(oldScore);
            String old = RedisService.beanToString(userScored);

            System.out.println(old);
            
            redisService.delList(UserPrefix.userScoreList,user.getId().toString(),old);
            redisService.setListRedis(UserPrefix.userScoreList,user.getId().toString(),newScore);

            //用 id 来代替标记，是新增 or 修改分数：
            userScored.setId(-1l);

            //同时，由于修改评分电影总分那边也要计算，因此，将两次评分差值传递过去就行了：
            userScored.setScore(score - oldScore);

        }else{      //新增评分
            redisService.setListRedis(UserPrefix.userScoreList,user.getId().toString(),newScore);
            userScored.setId(1l);


        }

        userScored.setUserId(user.getId());
        newScore = RedisService.beanToString(userScored);
        //mq 发送，更改数据库：
        mqService.updateMsg(userScoreQueue,newScore);


        return 0;
    }


    /**
     * 新增电影
     * @param movie
     * @return
     */
    @Override
    public int upLoadMovie(UploadMovie movie) {
        MovieDetail movieDetail = UploadMovieToMD(movie);
        int res = movieDetailMapper.insert(movieDetail);

        Long ID= movieDetail.getId();

        if(insertIntoMovie(ID) < 0 ){
            return 0;
        }

        return res;
    }

    /**
     * 更新电影信息
     * @param movie
     * @param id
     * @return
     */
    @Override
    public int updateMovie(UploadMovie movie, Long id) {
        MovieDetail movieDetail = UploadMovieToMD(movie);
        movieDetail.setId(id);

        String msg = RedisService.beanToString(movieDetail);

//        int res = movieDetailMapper.updateById(movieDetail);
        mqService.updateMsg(movieDQueue,msg);
        return  1   ;
    }

    /**
     * 删除
     * @param id
     * @param path
     * @return
     */
    @Override
    public int deleteMovie(Long id) {
        String path =  getMovieDetail(id).getPost();
        int res = movieDetailMapper.deleteById(id);
        qiniuService.deleteFile(path);

        return res;
    }

    @Override
    public int deleteMovieList(JSONArray array) {
        for (int i = 0; i <array.size() ; i++) {
            Long id = Long.parseLong(String.valueOf(array.get(i)));
            String path =  getMovieDetail(id).getPost();
            int res = movieDetailMapper.deleteById(id);
            qiniuService.deleteFile(path);
        }
        return 1;
    }

    //    ------------------------------------------------------------------------------------------------------------------------

    /**
     * 判断是否收藏电影：
     *
     * @param
     * @return
     */
    private boolean judgeIsCollected(Long user_id,Long movie_id) {
        //取出收藏列表：
//        List<Collections> collections = redisService.getList(UserPrefix.userCollections,String.valueOf(UserThreadLocal.get().getId()),Collections.class);
        List<Long> list = redisService.getListRedis(UserPrefix.userCollections, String.valueOf(user_id), Long.class);

        list.forEach(System.out::println);

        for (int i = 0; i < list.size(); i++) {
            if(list.get(i) == movie_id){
                return true;
            }
        }
        return false;
    }

    /**
     * 拿到 评分数据：
     *
     * @param list 查找出来的未判断
     * @return
     */
    private int getMyScore(Long user_id,Long movie_id) {
        //取出用户评分列表：
//        List<Collections> collections = redisService.getList(UserPrefix.userCollections,String.valueOf(UserThreadLocal.get().getId()),Collections.class);
        List<UserScored> list = redisService.getListRedis(UserPrefix.userScoreList, String.valueOf(user_id), UserScored.class);

        list.forEach(System.out::println);


        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).getMovieId().equals(movie_id)){
                return list.get(i).getScore();
            }
        }
        return 0;
    }


    /**
     * 从数据库取出电影详情：
     *
     * @param movie_id
     * @param user_id
     * @return
     */
    public MoviePage getMoviePageDB(Long movie_id, Long user_id) {
        MoviePage moviePage = movieDetailMapper.getDetail(movie_id);

        QueryWrapper<UserScored> wrapper = new QueryWrapper();   //找出用户有没有评分过
        wrapper.eq("user_id", user_id)
                .and(c -> c.eq("movie_id", movie_id));


        List<Collections> collections = redisService.getList(UserPrefix.userCollections, String.valueOf(user_id), Collections.class);
        if (collections != null) {
            for (int i = 0; i < collections.size(); i++) {
                if (collections.get(i).getMovieId() == movie_id) {
                    moviePage.setCollected(true);
                }
            }
        }

        return moviePage;
    }

    public MovieDetail UploadMovieToMD(UploadMovie movie){
        MovieDetail movieDetail = new MovieDetail();
        movieDetail.setName(movie.getName());
        movieDetail.setEnglishName((movie.getEnglishName()));
        movieDetail.setCountry(movie.getArea());
        movieDetail.setDirector(movie.getDirector());
        movieDetail.setWriter(movie.getWriter());
        movieDetail.setLength(movie.getLength());
        movieDetail.setDoubanDataId(-1);
        movieDetail.setSummary(movie.getSummary());
        movieDetail.setLanguage(movie.getLanguage());

        //主演类型转化:
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < movie.getStaring().size(); i++) {
            if(i !=  0){
                stringBuilder.append(" / ");
            }
            stringBuilder.append(movie.getStaring().get(i));
        }
        movieDetail.setStaring(stringBuilder.toString());

        //类型 ：
        stringBuilder = new StringBuilder();
        for (int i = 0; i < movie.getGenres().size(); i++) {
            if(i !=  0){
                stringBuilder.append("/");
            }
            stringBuilder.append(movie.getStaring().get(i));
        }
        movieDetail.setGenres(stringBuilder.toString());

        //时间转换：
        int year = Integer.parseInt(String.format("%tY",movie.getReleaseDate()));
        movieDetail.setReleaseDate(year);

        //海报上传：
        if(movie.getPost() != null){
            try {
                String path = qiniuService.saveImage(movie.getPost());
                System.out.println("path:"+path);
                movieDetail.setPost(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        return movieDetail;
    }

    public int insertIntoMovie(Long ID){

        System.out.println("ID:"+ID);
        Movie movie = new Movie();
        movie.setMovieId(ID);
        movie.setScore(0);
        movie.setPutOnDate(new Date());

        return movieMapper.insert(movie);
    }

}
