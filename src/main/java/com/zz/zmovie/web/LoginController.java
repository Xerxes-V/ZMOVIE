package com.zz.zmovie.web;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zz.zmovie.service.UserService;
import com.zz.zmovie.mapper.UserMapper;
import com.zz.zmovie.po.User;
import com.zz.zmovie.utils.ResultGeekQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;

import static com.zz.zmovie.utils.ResultStatus.*;

@RestController
@CrossOrigin
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResultGeekQ<Boolean> register(HttpServletResponse response,@RequestBody Map<String,Object> map){
        ResultGeekQ<Boolean> res = ResultGeekQ.done(REGISTER_SUCCESS);

        if(!userService.register(response,map.get("account").toString(),map.get("password").toString())){
            return ResultGeekQ.error(REGISTER_FAILURE);
        }

        return res;
    }

    @PostMapping("/doLogin")
    public ResultGeekQ<Boolean> doLogin(HttpServletResponse response,@RequestBody Map<String,Object> map){



        int res = userService.doLogin(response,map.get("account").toString(),map.get("password").toString());


        if( res == 1){
            return  ResultGeekQ.done(LOGIN_SUCCESS,true);
        }else if(res == 2) {
            return  ResultGeekQ.done(EMPTY_LABEL,true);
        }
        return ResultGeekQ.error(LOGIN_FAILURE,false);
    }

    /**
     * 登出，删除 redis 的 token
     * @return
     */
    @GetMapping("/logout")
    public ResultGeekQ<Boolean> logOut(HttpServletResponse response, HttpServletRequest request){
        if(userService.logout(response,request)){
            return ResultGeekQ.done(SUCCESS,true);
        }
        return ResultGeekQ.error(FAILURE,false);
    }


    @GetMapping("/test")
    public User test(){
        QueryWrapper<User> queryWrapper = new QueryWrapper();
        queryWrapper.eq("account","helle");
        User user = userMapper.selectOne(queryWrapper);

        return user;
    }
}
