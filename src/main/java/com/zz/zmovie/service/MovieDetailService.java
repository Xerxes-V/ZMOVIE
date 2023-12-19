package com.zz.zmovie.service;

import cn.hutool.json.JSONArray;
import com.zz.zmovie.vo.MoviePage;
import com.zz.zmovie.vo.admin.UploadMovie;

public interface MovieDetailService {
    int doCollect(boolean isCollected,Long movie_id );

    MoviePage getMovieDetail(Long movie_id);

    int updateScore(Long movie_id,int score,int oldScore);

    int upLoadMovie(UploadMovie movie);

    int updateMovie(UploadMovie movie,Long id);

    int deleteMovie(Long id);

    int deleteMovieList(JSONArray array);
}
