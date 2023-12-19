package com.zz.zmovie.service;

import com.zz.zmovie.po.Comments;
import com.zz.zmovie.po.MovieComments;
import com.zz.zmovie.po.Report;
import com.zz.zmovie.po.User;
import com.zz.zmovie.vo.admin.MovieMsg;
import com.zz.zmovie.vo.admin.UploadMovie;
import com.zz.zmovie.vo.admin.UserMsg;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface AdminService {

      boolean doLogin(HttpServletResponse response, String account, String password);

      UserMsg getUserMsg();

      MovieMsg getMovieMsg();

      Map<String,Object> getMovieManagement(int start);

      UploadMovie getMovieData(Long id);

      Map<String,Object> searchMovieManagement(String name,int start);

      List<MovieComments> getMC();

      List<Comments> getReplys();

      List<User> getU();

      List<Report> getReport();

      int delReport(Long id);

      int deleteSame(Long commentId,int type);

      int banU(Long userId);
}
