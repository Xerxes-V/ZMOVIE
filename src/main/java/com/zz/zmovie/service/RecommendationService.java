package com.zz.zmovie.service;

import com.zz.zmovie.vo.TopMovies;

import java.util.List;

public interface RecommendationService {

//    List<Long> itemBasedRecommender(long userID, int size);

   void getRecommends(Long userId);

    void getRecommendsMaHout(Long userId);
}
