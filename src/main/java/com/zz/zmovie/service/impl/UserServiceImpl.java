package com.zz.zmovie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zz.zmovie.interceptor.UserInterceptor;
import com.zz.zmovie.rabbitMq.MqService;
import com.zz.zmovie.service.UserService;
import com.zz.zmovie.config.UserThreadLocal;
import com.zz.zmovie.exception.GlobleException;
import com.zz.zmovie.mapper.CollectionsMapper;
import com.zz.zmovie.mapper.UserMapper;
import com.zz.zmovie.mapper.UserScoredMapper;
import com.zz.zmovie.po.Collections;
import com.zz.zmovie.po.User;
import com.zz.zmovie.po.UserScored;
import com.zz.zmovie.redis.RedisService;
import com.zz.zmovie.redis.UserPrefix;
import com.zz.zmovie.utils.MD5Utils;
import com.zz.zmovie.utils.QiniuService;
import com.zz.zmovie.utils.UUIDUtil;
import com.zz.zmovie.vo.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.zz.zmovie.utils.ResultStatus.*;

@Service
public class UserServiceImpl implements UserService {


    private static final String salt = "1a2b3c";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private CollectionsMapper collectionsMapper;

    @Autowired
    private UserScoredMapper userScoredMapper;

    @Autowired
    private MqService mqService;
    
    @Autowired
    private UserInterceptor userInterceptor;

    @Autowired
    private QiniuService qiniuService;

    private static final String userQueue = "user";

    /**
     * 根据 token 信息从redis查找出用户信息并放置到 ThreadLocal
     * @param response
     * @param token
     * @return
     */
    @Override
    public User getByToken(HttpServletResponse response, String token) {
        User user = redisService.get(UserPrefix.userToken,token,User.class);

        if(user != null){
            addToCookie(response,token,user);
        }

        return  user;
    }

    /**
     * 登录逻辑
     * @param response
     * @param account
     * @param password
     * @return
     */
    @Override
    public int doLogin(HttpServletResponse response,String account, String password) {

        User user = getUserByAccount(account);      //取出用户
//        md5加密并判断：
        String dbPass = MD5Utils.formPassToDBPass(password, salt);
        if ( !dbPass.equals(user.getPassword()) ) {
            throw new GlobleException(PWD_WRONG);
        }

        if(user.getStatus() < 0){
            throw new GlobleException(USER_STATUS_BAN);
        }


        //生成 用户 Token ：
        String token = UUIDUtil.uuid();     //隨機值
        String authority_token = "1"+token;         //前面添加一个数字作为用户的权限，1为用户，9为管理员


        //判断 收藏列表、评分列表是否已经放到redis 当中，无则数据库取出并且添加：
        doCollections(user.getId());
        uploadScoreList(user.getId());


//       会生成一个以uuid为value，"token"为key的cookie， 并将以MiaoShaUserKey:tk +对应的md5码为key，user信息为value写入redis当中
        addToCookie(response,authority_token,user);

        if(user.getLabel() == null){
            return 2;       //需要弹出类型选择
        }

        return 1;
    }

    /**
     * 注册逻辑
     * @param response
     * @param account
     * @param password
     * @return
     */
    @Override
    public boolean register(HttpServletResponse response,String account, String password) {

        QueryWrapper<User> queryWrapper = new QueryWrapper();
        queryWrapper.eq("account",account);
        User user = userMapper.selectOne(queryWrapper);

        System.out.println(user);
        if(user != null){
            return false;
        }

        password = MD5Utils.formPassToDBPass(password,salt);

        user = new User(account,password);
        Date date = new Date();
        user.setCreateTime(date);
        userMapper.insert(user);

        return true;
    }

    /**
     * 修改密码
     * @param pwd
     * @return
     */
    @Override
    public int updatePwd(@RequestParam String pwd) {

        User user = UserThreadLocal.get();
        System.out.println("u:"+user);


        User newUser = new User();
        newUser.setId(user.getId());
        String saltPwd = MD5Utils.formPassToDBPass(pwd,salt);

        newUser.setPassword(saltPwd);
        user.setPassword(saltPwd);

        //redis 先修改：
        if(!redisService.update(UserPrefix.getUserByAccount,user.getAccount(),user)){
            throw new GlobleException(UPDATE_FAILURE);
        }

        //mq 发送修改请求
        String userMsg = redisService.beanToString(user);
        return mqService.updateMsg(userQueue,userMsg);
    }

    /**
     * 登出，删除 redis 的 token
     * @return
     */
    @Override
    public boolean logout(HttpServletResponse response, HttpServletRequest request) {
        User user = UserThreadLocal.get();


        Thread thread = new Thread(
            ()->{
            System.out.println(String.valueOf(user.getId()));
                redisService.del(UserPrefix.userScoreList,String.valueOf(user.getId()));
                redisService.del(UserPrefix.userCollections,String.valueOf(user.getId()));
            }
        );
        String cookie = userInterceptor.getCookie(response,request);
        System.out.println(cookie);
        redisService.del(UserPrefix.userToken,cookie);
        thread.start();

        return true;

    }

    /**
     * 获取用户个人信息
     * @return
     */
    @Override
    public UserData getUserData() {
        User user = UserThreadLocal.get();

        UserData res = userMapper.getUserData(user.getId());
        //设置收藏的数量：

        int collectNum = Integer.parseInt(String.valueOf(redisService.getListLen(UserPrefix.userCollections,String.valueOf(user.getId()))));
        res.setCollectNum(collectNum);

        return res;
    }

    /**
     * 更改用户名
     * @return
     */
    @Override
    public int updateUserName(String name,HttpServletRequest request, HttpServletResponse response) {
       User user = UserThreadLocal.get();

       user.setUsername(name);


        return updateUserMsg(user,request,response);
    }

    /**
     * 更改用户头像
     * @param path
     * @return
     */
    @Override
    public int updateAvatar(String path,HttpServletRequest request, HttpServletResponse response) {
        User user = UserThreadLocal.get();

        String oldAvatar = user.getAvatar();
//        System.out.println(oldAvatar);
        if(oldAvatar != null){
            qiniuService.deleteFile(oldAvatar);
        }

        user.setAvatar(path);

        return updateUserMsg(user,request,response);
    }

    @Override
    public int setTypes(String types, HttpServletRequest request, HttpServletResponse response) {
        User user = UserThreadLocal.get();

        user.setLabel(types);

        redisService.update(UserPrefix.getUserByAccount,user.getAccount(),user);

        return updateUserMsg(user,request,response);
    }


    //    --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    

    /**
     * 根据账户寻找用户，先 redis 后数据库
     *
     * @param account
     * @return
     */
    public User getUserByAccount(String account) {
        //先从redis中寻找：
        User user = redisService.get(UserPrefix.getUserByAccount,account,User.class);
        if(user != null){
            return user;
        }

//        数据库：
        QueryWrapper<User> queryWrapper = new QueryWrapper();
        queryWrapper.eq("account", account);
        user = userMapper.selectOne(queryWrapper);     //数据库取出数据
        if (user == null) {
            throw new GlobleException(LOGIN_EMPTY_ACCOUNT);
        }
        redisService.set(UserPrefix.getUserByAccount,account,user);
        return user;
    }


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

    /**
     * 从数据库中查找出用户的收藏列表 并添加到redis
     * @param id
     */
    public void doCollections(Long id){
        if(redisService.existKey(UserPrefix.userCollections,String.valueOf(id))){
//            System.out.println();
            return;
        }

        QueryWrapper<Collections> queryWrapper = new QueryWrapper();        //只用存 电影 id
        queryWrapper.select("movie_id")
                    .eq("user_id",id);


        List<Collections> collections = collectionsMapper.selectList(queryWrapper);
        List<Long> ids = collections.stream()                           //提取出 id
                                    .map(Collections::getMovieId)
                                    .collect(Collectors.toList());

        //加入到 redis
        redisService.setListByList(UserPrefix.userCollections,String.valueOf(id),ids);
    }

    /**
     * 从数据库中查找出用户的评分记录 并添加到redis
     * @param id
     */
    public void uploadScoreList(Long id){
        System.out.println("?");
        if(redisService.existKey(UserPrefix.userScoreList,String.valueOf(id))){
            return;
        }

        QueryWrapper<UserScored> queryWrapper = new QueryWrapper();        //只用存 电影 id
        queryWrapper.select("movie_id","score")
                    .isNotNull("movie_id")
                    .eq("user_id",id);


        List<UserScored> userScoreds = userScoredMapper.selectList(queryWrapper);


        //加入到 redis
        redisService.setListByList(UserPrefix.userScoreList,String.valueOf(id),userScoreds);
    }

    public int updateUserMsg(User user,HttpServletRequest request, HttpServletResponse response){
        //redis 中修改:
        String cookie = userInterceptor.getCookie(response,request);
        System.out.println(cookie);
        redisService.update(UserPrefix.userToken,cookie,user);

        String msg = RedisService.beanToString(user);

        //mq发更改:
        mqService.updateMsg(userQueue,msg);
        return 1;
    }


}
