package com.zz.zmovie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zz.zmovie.po.MovieDetail;
import com.zz.zmovie.vo.SearchMovie;
import com.zz.zmovie.vo.TopMovies;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MovieService {
    List<TopMovies> getHottest();

    List<TopMovies> getNewest(int start);

    List<TopMovies> getMostComment();

    List<TopMovies> getMostCollected();

    Map<String,Object> getNew36(int curPage);

    Page<MovieDetail> screen(Map<String, Object> map);

    Set<SearchMovie> searchMovie(String msg);

    List<MovieDetail> viewCollections();

    List<MovieDetail> viewHistory();

    List<TopMovies> getRecommendation();


    //    List<Integer> getHottestId();

}
