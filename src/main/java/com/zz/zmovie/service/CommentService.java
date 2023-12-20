package com.zz.zmovie.service;

import com.zz.zmovie.vo.AllComments;
import com.zz.zmovie.vo.MyComments;
import com.zz.zmovie.vo.MyMovieComments;
import com.zz.zmovie.vo.SonsComments;

import java.util.List;
import java.util.Map;

public interface CommentService {
    List<AllComments> getAllComments(Long movie_id, int start);

    AllComments publishMovieComment(Map<String, Object> map);

    SonsComments reply(Map<String, Object> map);

    int deleteMovieComment(Long comment_id);

    int deleteReply(Long reply_id);

    List<MyMovieComments> getPersonalMovieComments();

    List<MyComments> viewPersonalComments();

    int report(Long comment_id, int type, String message);

    int like(Long comment_id, int type, int like, int past);

}
