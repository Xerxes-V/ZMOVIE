package com.zz.zmovie.web;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.zz.zmovie.po.Comments;
import com.zz.zmovie.po.MovieComments;
import com.zz.zmovie.po.Report;
import com.zz.zmovie.po.User;
import com.zz.zmovie.service.AdminService;
import com.zz.zmovie.service.MovieDetailService;
import com.zz.zmovie.utils.ResultGeekQ;
import com.zz.zmovie.utils.ResultStatus;
import com.zz.zmovie.vo.admin.MovieMsg;
import com.zz.zmovie.vo.admin.UploadMovie;
import com.zz.zmovie.vo.admin.UserMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static com.zz.zmovie.utils.ResultStatus.FAILURE;
import static com.zz.zmovie.utils.ResultStatus.SUCCESS;

@RestController
@CrossOrigin
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private MovieDetailService movieDetailService;

    @PostMapping("/log")
    public ResultGeekQ<Boolean> doLogin(HttpServletResponse response, @RequestBody Map<String, Object> map){
        System.out.println(map.get("account"));
        System.out.println(map.get("password"));

        if(adminService.doLogin(response,String.valueOf(map.get("account")),String.valueOf(map.get("password")))){
            return ResultGeekQ.done(ResultStatus.LOGIN_SUCCESS,true);
        }

        return ResultGeekQ.error(ResultStatus.LOGIN_FAILURE,false);
    }

    @GetMapping("/userMsg")
    public ResultGeekQ<UserMsg> getUserMsg(){
        UserMsg res = adminService.getUserMsg();
        if(res != null){
            return ResultGeekQ.done(SUCCESS,res);
        }

        return ResultGeekQ.error(FAILURE,null);
    }

    @GetMapping("/movieMsg")
    public ResultGeekQ<MovieMsg> getMovieMsg(){
        MovieMsg res = adminService.getMovieMsg();
        if(res != null){
            return ResultGeekQ.done(SUCCESS,res);
        }

        return ResultGeekQ.error(FAILURE,null);
    }

    @PostMapping("/uploadMovie")
    public ResultGeekQ<Boolean> uploadMovie(@RequestParam(value = "post") MultipartFile post,@RequestParam(value = "movieMessage") String str ){

        UploadMovie uploadMovie = JSONUtil.toBean(str,UploadMovie.class);
        uploadMovie.setPost(post);
        System.out.println(uploadMovie);

        int res = movieDetailService.upLoadMovie(uploadMovie);
        if(res > 0){
            return ResultGeekQ.done(SUCCESS,true);
        }

        return ResultGeekQ.error(FAILURE,false);
    }

    @GetMapping("/movieManagement")
    public ResultGeekQ<Map<String,Object>> getMovieManagement(){
        Map<String,Object> res = adminService.getMovieManagement(0);

        if(res != null){
            return ResultGeekQ.done(SUCCESS,res);
        }

        return ResultGeekQ.error(FAILURE,null);
    }

    @GetMapping("/movieData/{id}")
    public ResultGeekQ<UploadMovie> getMovieData(@PathVariable Long id){
        UploadMovie res = adminService.getMovieData(id);

        if(res != null){
            return ResultGeekQ.done(SUCCESS,res);
        }

        return ResultGeekQ.error(FAILURE,null);
    }

    @PutMapping("/updateMovie")
    public ResultGeekQ<Boolean> updateMovie(@RequestParam(value = "post",required = false) MultipartFile post,@RequestParam(value = "movieMessage") String str,@RequestParam(value = "id") String id ){

        UploadMovie uploadMovie = JSONUtil.toBean(str,UploadMovie.class);
        uploadMovie.setPost(post);

        System.out.println(uploadMovie);

        int res = movieDetailService.updateMovie(uploadMovie,Long.parseLong(id));
        if(res > 0){
            return ResultGeekQ.done(SUCCESS,true);
        }

        return ResultGeekQ.error(FAILURE,false);
    }

    @DeleteMapping("/movieDelete/{id}")
    public ResultGeekQ<Boolean> movieDelete( @PathVariable Long id) {
        int res = movieDetailService.deleteMovie(id);

        if (res > 0) {
            return ResultGeekQ.done(SUCCESS, true);
        }

        return ResultGeekQ.error(FAILURE, false);

    }


    /**
     * 批量删除
     * @param id
     * @return
     */
    @DeleteMapping("/movieDeleteList")
    public ResultGeekQ<Boolean> movieDeleteList( @RequestBody Map<String,Object> map) {

        JSONArray array = JSONUtil.parseArray((map.get("ids")));

        int res = movieDetailService.deleteMovieList(array);

        if (res > 0) {
            return ResultGeekQ.done(SUCCESS, true);
        }

        return ResultGeekQ.error(FAILURE, false);
    }

    /**
     * 搜电影
     * @param name
     * @return
     */
    @GetMapping("/searchMovie")
    public ResultGeekQ<Map<String,Object>> search(@RequestParam(value="name") String name){
        Map<String,Object> res = adminService.searchMovieManagement(name,0);

        if(res != null){
            return ResultGeekQ.done(SUCCESS,res);
        }

        return ResultGeekQ.error(FAILURE,null);
    }

    /**
     * 换页操作
     * @param page
     * @return
     */
    @GetMapping("/changePage")
    public ResultGeekQ<Map<String,Object>> changePage(@RequestParam(value="page") int page){
        Map<String,Object> res = adminService.getMovieManagement(page-1);

        if(res != null){
            return ResultGeekQ.done(SUCCESS,res);
        }

        return ResultGeekQ.error(FAILURE,null);
    }

    /**
     * 带着搜索搜索结果进行换页
     * @param name
     * @param page
     * @return
     */
    @GetMapping("/searchAndChange")
    public ResultGeekQ<Map<String,Object>> searchAndChange(@RequestParam(value="name") String name,@RequestParam(value="page") int page){
        Map<String,Object> res = adminService.searchMovieManagement(name,page-1);

        if(res != null){
            return ResultGeekQ.done(SUCCESS,res);
        }

        return ResultGeekQ.error(FAILURE,null);
    }

    /**
     * 获取影评
     * @return
     */
    @GetMapping("/movieComments")
    public ResultGeekQ<List<MovieComments>> getMC(){
        List<MovieComments> res = adminService.getMC();

        return ResultGeekQ.done(SUCCESS,res);
    }

    /**
     * 获取回复
     * @return
     */
    @GetMapping("/replys")
    public ResultGeekQ<List<Comments>> getReplys(){
        List<Comments> res = adminService.getReplys();

        return ResultGeekQ.done(SUCCESS,res);
    }


    /**
     * 获取用户信息
     * @return
     */
    @GetMapping("/users")
    public ResultGeekQ<List<User>> getUsers(){
        List<User> res = adminService.getU();

        return ResultGeekQ.done(SUCCESS,res);
    }

    /**
     * 获取举报信息
     */

    @GetMapping("/reports")
    public ResultGeekQ<List<Report>> getReports(){
        List<Report> res  = adminService.getReport();

        if(res != null){
            return ResultGeekQ.done(SUCCESS,res);
        }

        return ResultGeekQ.error(FAILURE);
    }

    /**
     * 删除举报信息
     */

    @DeleteMapping("/delReport/{id}")
    public ResultGeekQ<Boolean> delReport(@PathVariable Long id){
        int res = adminService.delReport(id);
        if(res > 0){
            return ResultGeekQ.done(SUCCESS,true);
        }

        return ResultGeekQ.error(FAILURE,false);
    }

    /**
     * 删除所有的评论id 相同的举报信息
     * @param map
     * @return
     */
        @DeleteMapping("/deleteSame")
    public ResultGeekQ<Boolean> delReport(@RequestBody Map<String,Object> map){
        Long commentId = Long.parseLong(String.valueOf(map.get("commentId")));
        int type = Integer.parseInt(String.valueOf(map.get("type")));


        int res = adminService.deleteSame(commentId,type);
        if(res > 0){
            return ResultGeekQ.done(SUCCESS,true);
        }

        return ResultGeekQ.error(FAILURE,false);
    }

    /**
     * 封禁 or 解封
     */
    @GetMapping("/ban/{userId}")
    public ResultGeekQ<Boolean> banUser(@PathVariable Long userId){
        int res = adminService.banU(userId);

        if(res > 0){
           return ResultGeekQ.done(SUCCESS,true);
        }
        return ResultGeekQ.error(FAILURE,false);
    }

}
