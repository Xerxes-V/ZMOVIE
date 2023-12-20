package com.zz.zmovie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zz.zmovie.service.AdminService;
import com.zz.zmovie.exception.GlobleException;
import com.zz.zmovie.mapper.*;
import com.zz.zmovie.po.*;
import com.zz.zmovie.redis.AdminPrefix;
import com.zz.zmovie.redis.RedisService;
import com.zz.zmovie.redis.UserPrefix;
import com.zz.zmovie.utils.MD5Utils;
import com.zz.zmovie.utils.UUIDUtil;
import com.zz.zmovie.vo.admin.MovieManagement;
import com.zz.zmovie.vo.admin.MovieMsg;
import com.zz.zmovie.vo.admin.UploadMovie;
import com.zz.zmovie.vo.admin.UserMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.zz.zmovie.utils.ResultStatus.PWD_WRONG;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private CommentsMapper commentsMapper;

    @Autowired
    private MovieCommentsMapper movieCommentsMapper;

    @Autowired
    private MovieDetailMapper movieDetailMapper;

    @Autowired
    private MovieMapper movieMapper;

    @Autowired
    private UserScoredMapper userScoredMapper;

    @Autowired
    private ReportMapper reportMapper;


    private static final String salt = "1a2b3c";

    /**
     * 登录逻辑
     * @param response
     * @param account
     * @param password
     * @return
     */
    @Override
    public boolean doLogin(HttpServletResponse response, String account, String password) {

        QueryWrapper<User> queryWrapper = new QueryWrapper();
        queryWrapper.eq("account",account);
        User user = userMapper.selectOne(queryWrapper);

//        md5加密并判断：
        String dbPass = MD5Utils.formPassToDBPass(password, salt);
        if ( !dbPass.equals(user.getPassword()) ) {
            throw new GlobleException(PWD_WRONG);
        }

        //生成 用户 Token ：
        String token = UUIDUtil.uuid();     //隨機值
        String authority_token = "9"+token;         //前面添加一个数字作为用户的权限，1为用户，9为管理员


//       会生成一个以uuid为value，"token"为key的cookie， 并将以MiaoShaUserKey:tk +对应的md5码为key，user信息为value写入redis当中
        addToCookie(response,authority_token,user);
        return true;
    }

    /**
     * 获取 所有用户 统计信息
     * @return
     */
    @Override
    public UserMsg getUserMsg() {
        UserMsg userMsg = redisService.get(AdminPrefix.adminUserMsg,"userMsg",UserMsg.class);
        if(userMsg != null){
            return userMsg;
        }

        userMsg = getUserMsgFromDB();
        redisService.set(AdminPrefix.adminUserMsg,"userMsg",userMsg);

        return userMsg;

    }

    /**
     * 获取 所有的 电影统计信息
     * @return
     */
    @Override
    public MovieMsg getMovieMsg() {
        MovieMsg movieMsg = redisService.get(AdminPrefix.adminMovieMsg,"movieMsg",MovieMsg.class);
        if(movieMsg != null){
            return movieMsg;
        }

        movieMsg = getMovieMsgFromDB();
        redisService.set(AdminPrefix.adminMovieMsg,"movieMsg",movieMsg);
        return movieMsg;
    }

    /**
     * 获取管理员界面下的管理电影信息
     * @return
     */
    @Override
    public Map<String,Object> getMovieManagement(int start) {
        start = start * 12;
        List<MovieManagement> res = movieDetailMapper.getMovieManagement(start);

        QueryWrapper<MovieDetail> queryWrapper = new QueryWrapper<>();
        int movieNum = movieDetailMapper.selectCount(queryWrapper);

        Map<String,Object> map = new HashMap<>();
        map.put("movie",res);
        map.put("counts",movieNum);

        return map;
    }

    /**
     * 获取电影信息放到编辑页面
     * @return
     */
    @Override
    public UploadMovie getMovieData(Long id){
        MovieDetail movieDetail = movieDetailMapper.selectById(id);

        UploadMovie movie = new UploadMovie();
        movie.setArea(movieDetail.getCountry());
        movie.setDirector(movieDetail.getDirector());
        movie.setEnglishName(movieDetail.getEnglishName());
        movie.setLength(movieDetail.getLength());
        movie.setName(movieDetail.getName());
        movie.setLanguage(movieDetail.getLanguage());
        movie.setWriter(movieDetail.getWriter());
        movie.setSummary(movieDetail.getSummary());
        movie.setPath(movieDetail.getPost());

        //分类：
        String[] genres = movieDetail.getGenres().split("/");
        movie.setGenres(Arrays.asList(genres));

        //主演：
        String[] staring =  movieDetail.getStaring().split(" / ");
        movie.setStaring(Arrays.asList(staring));

        //上映时间：
        String time =  movieDetail.getReleaseDate()+"-1-1 00:00:00";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date newTime = format.parse(time);
            movie.setReleaseDate(newTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return movie;
    }

    /**
     * 根据电影名搜索电影
     * @param name
     * @return
     */
    @Override
    public Map<String,Object>  searchMovieManagement(String name,int start) {
        start = start * 12;
        List<MovieManagement> res = movieDetailMapper.searchMovieManagementByName("%"+name+"%",start);

        QueryWrapper<MovieDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name",name);
        int movieNum = movieDetailMapper.selectCount(queryWrapper);

        Map<String,Object> map = new HashMap<>();
        map.put("movie",res);
        map.put("counts",movieNum);
        return map;
    }

    @Override
    public List<MovieComments> getMC() {
        QueryWrapper<MovieComments> queryWrapper = new QueryWrapper();
        queryWrapper.orderByDesc("comment_time");


        List<MovieComments> res = movieCommentsMapper.selectList(queryWrapper);

        return res;
    }

    @Override
    public List<Comments> getReplys() {
        QueryWrapper<Comments> queryWrapper = new QueryWrapper();
        queryWrapper.orderByDesc("comment_time");


        List<Comments> res = commentsMapper.selectList(queryWrapper);

        return res;
    }

    @Override
    public List<User> getU() {
        QueryWrapper<User> queryWrapper = new QueryWrapper();
        queryWrapper.orderByDesc("create_time");
        queryWrapper.lt("status",4);

        List<User> res = userMapper.selectList(queryWrapper);

        return res;
    }

    @Override
    public List<Report> getReport() {
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
        List<Report> res = reportMapper.selectList(queryWrapper);
        return res;
    }

    @Override
    public int delReport(Long id) {
        int res = reportMapper.deleteById(id);

        return res;
    }

    @Override
    public int deleteSame(Long commentId,int type) {
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type",type);
        queryWrapper.eq("comment_id",commentId);

        int res = reportMapper.delete(queryWrapper);
        return res;

    }

    @Override
    public int banU(Long userId) {
        int status = userMapper.selectById(userId).getStatus();
        if(status >= 0){
            status = -1;
        }else{
            status = 1;
        }

        User user = new User();
        user.setId(userId);
        user.setStatus(status);

        int res = userMapper.updateById(user);
        return res;
    }

    //    ------------------------------------------------------------------------------------------------------------------------------------------------------------------
    /**
     * 将 用户token 放到redis以及cookie
     * @param response
     * @param token
     * @param user
     */
    public void addToCookie(HttpServletResponse response,String token,User user){
        redisService.set(UserPrefix.userToken,token,user);

        Cookie cookie = new Cookie("token",token);
        cookie.setMaxAge(UserPrefix.userToken.getExpireSeconds());
        cookie.setPath("/");
        cookie.setHttpOnly(false);


        response.addCookie(cookie);     //存放到客户端
    }

    //从数据库获取用户统计信息
    public UserMsg getUserMsgFromDB(){
        //用户数量：
        QueryWrapper<User> queryWrapper = new QueryWrapper();
        int userNum = userMapper.selectCount(queryWrapper);

        //影评数
        QueryWrapper<MovieComments> movieCommentsQueryWrapper = new QueryWrapper<>();
        int mcNum = movieCommentsMapper.selectCount(movieCommentsQueryWrapper);

        //评论数：
        QueryWrapper<Comments> commentsQueryWrapper = new QueryWrapper<>();
        int commentNum = commentsMapper.selectCount(commentsQueryWrapper);

        //违规用户数：
        queryWrapper.lt("status",0);
        int banUserNum = userMapper.selectCount(queryWrapper);

        //七天回复变化数据：
        List<Integer> commentNumsChange = commentsMapper.getEverydayNums();
        int sevenDaysAgo = commentsMapper.getSevenDaysAgoNums();
        commentNumsChange = get7DysData(commentNumsChange,sevenDaysAgo);

        //七天影评变化数据：
        List<Integer> mcChangeNum = movieCommentsMapper.getSevenDaysChangeNum();
        int mcSevenDaysAgo = movieCommentsMapper.getSevenDaysAgoNum();
        mcChangeNum = get7DysData(mcChangeNum,mcSevenDaysAgo);

        UserMsg userMsg = new UserMsg();
        userMsg.setUserNum(userNum);
        userMsg.setMovieCommentNum(mcNum);
        userMsg.setCommentNum(commentNum);
        userMsg.setBanUser(banUserNum);
        userMsg.setCommentNumChange(commentNumsChange);
        userMsg.setMovieCommentNumChange(mcChangeNum);
        return userMsg;
    }

    //从数据库获取电影统计信息
    public MovieMsg getMovieMsgFromDB(){
        //电影数量：
        QueryWrapper<MovieDetail> movieDetailQueryWrapper = new QueryWrapper<>();
        int movieNum = movieDetailMapper.selectCount(movieDetailQueryWrapper);

        //今日新增：
        int todayIncrease = movieMapper.getTodayIncrease();

        //今日最火（评分最多次数）：
        String movie_name = userScoredMapper.getHottest();

        //国家分布的电影数量： 台-陆-港-美
        List<Integer> moviesDifferentArea = movieDetailMapper.getMovieNumsByArea();
        int otherArea = 0;
        for (int j = 0; j < moviesDifferentArea.size(); j++) {
            otherArea+=moviesDifferentArea.get(j);
        }
        moviesDifferentArea.add(movieNum-otherArea);        //其他地区

        //评分分布：0-4，4-6，6-8，8-10：
        List<Integer> scoreNums = movieMapper.getNumsByScore();

        MovieMsg movieMsg = new MovieMsg();
        movieMsg.setMovieNum(movieNum);
        movieMsg.setIncreaseNum(todayIncrease);
        movieMsg.setHottestMovie(movie_name);
        movieMsg.setMovieAreaNum(moviesDifferentArea);
        movieMsg.setMovieMakeOfByStars(scoreNums);
        return movieMsg;
    }

    //获取 7 天内数量变化
    public List<Integer> get7DysData(List<Integer> list,int base){
        List<Integer> res = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            if(i == 0){
                res.add(base);
            }else{
                res.add(res.get(i-1) + list.get(i));
            }
        }
        return res;
    }


}
