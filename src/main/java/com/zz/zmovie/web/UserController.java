package com.zz.zmovie.web;

import com.zz.zmovie.service.UserService;
import com.zz.zmovie.config.UserThreadLocal;
import com.zz.zmovie.utils.QiniuService;
import com.zz.zmovie.utils.ResultGeekQ;
import com.zz.zmovie.utils.ResultStatus;
import com.zz.zmovie.vo.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static com.zz.zmovie.utils.ResultStatus.*;

@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private QiniuService qiniuService;
    
//    @PostMapping("/changeAvatar")
//    public void changeAvatar(MultipartFile file){
//        System.out.println(MultipartFileToFile.saveMultipartFile(file, "../MultipartFileDir"));
//        System.out.println(file);
//    }

    /**
     * 更改用户头像
     * @param file
     * @param request
     * @param response
     * @return
     */
    @PutMapping("/changeAvatar")
    public ResultGeekQ<String> changeAvatar(@RequestParam(value = "file") MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        try {
            String path = qiniuService.saveImage(file);
            System.out.println("path:"+path);

            int res = userService.updateAvatar(path,request,response);
            if(res > 0){
                return ResultGeekQ.done(SUCCESS,path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return ResultGeekQ.error(FAILURE,null);
    }

    /**
     * 修改密码
     * @param pwd
     * @return
     */
    @PutMapping("/updatepwd")
    public ResultGeekQ<Boolean> changePwd(String pwd){
        int res = userService.updatePwd(pwd);
        if(res < 0){
            return ResultGeekQ.error(UPDATE_FAILURE,false);
        }
        return ResultGeekQ.done(UPDATE_SUCCESS,true);
    }

    /**
     * 获取用户信息
     * @return
     */
    @GetMapping("/userData")
    public ResultGeekQ<UserData> getUserData(){
        UserData userData = userService.getUserData();
        if(userData != null){
            return ResultGeekQ.done(SUCCESS,userData);
        }

        return ResultGeekQ.error(FAILURE,null);
    }

    /**
     * 登录后展示用户头像
     * @return
     */
        @GetMapping("/getAvatar")
    public ResultGeekQ<String> getAvatar(){
        String avatar = UserThreadLocal.get().getAvatar();
        ResultStatus r = avatar==null?FAILURE:SUCCESS;
        return ResultGeekQ.done(r,avatar);
    }


    /**
     * 测试
     * @param file
     * @param name
     * @param url
     */
    @PostMapping("/ipt")
    public void ipt(@RequestParam(value = "file") MultipartFile file,@RequestParam(value = "name") String name,@RequestParam(value = "fff") String url){

        System.out.println(file);
        System.out.println(name);
        System.out.println(url);
    }

    /**
     * 更改用户名
     * @param name
     * @param request
     * @param response
     * @return
     */
    @PutMapping("/changeUsername")
    public ResultGeekQ<Boolean> changeUsername(@RequestParam(value = "newUsername") String name, HttpServletRequest request, HttpServletResponse response){
        System.out.println(name);

        if(userService.updateUserName(name,request,response) > 0){
            return ResultGeekQ.done(SUCCESS,true);
        }
        return ResultGeekQ.error(FAILURE,false);
    }


    /**
     * 初次登录，类型选择：
     */
    @PostMapping("/selectTypes")
    public ResultGeekQ<Boolean> setTypes(@RequestBody Map<String,Object> map, HttpServletRequest request, HttpServletResponse response){
        int res = userService.setTypes(String.valueOf(map.get("types")),request,response);

        if(res > 0){
            return ResultGeekQ.done(SUCCESS,true);
        }

        return ResultGeekQ.error(FAILURE,false);
    }



}
