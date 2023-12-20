package com.zz.zmovie.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zz.zmovie.service.MovieDetailService;
import com.zz.zmovie.service.MovieService;
import com.zz.zmovie.service.RecommendationService;
import com.zz.zmovie.service.impl.RecommendationServiceImpl;
import com.zz.zmovie.mapper.MovieDetailMapper;
import com.zz.zmovie.po.MovieDetail;
import com.zz.zmovie.utils.ResultGeekQ;
import com.zz.zmovie.vo.MoviePage;
import com.zz.zmovie.vo.SearchMovie;
import com.zz.zmovie.vo.TopMovies;
import org.apache.mahout.cf.taste.common.TasteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.zz.zmovie.utils.ResultStatus.*;

@RestController
@CrossOrigin
@RequestMapping("/movie")
public class MovieController {

    @Autowired
    private MovieDetailMapper movieDetailMapper;

    @Autowired
    private MovieService movieService;

    @Autowired
    private MovieDetailService movieDetailService;

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/test")
    public Object testMD(){
        Object res = movieDetailMapper.selectById(1);
        return res;
    }



    /**
     * 获取最热列表
     * @return
     */
    @GetMapping("/hot")
    public ResultGeekQ<List<TopMovies>> hot(){
        List<TopMovies> res = movieService.getHottest();

        if(res != null){
            return ResultGeekQ.done(SUCCESS,res);
        }

        return ResultGeekQ.error(FAILURE);
    }

    /**
     * 获取最新电影，start 为第几页
     * @param start
     * @return
     */
    @GetMapping("/new/{start}")
    public ResultGeekQ<List<TopMovies>> getNew(@PathVariable int start){
        List<TopMovies> res = movieService.getNewest(start-1);

        if(res != null){
            return ResultGeekQ.done(SUCCESS,res);
        }
        return ResultGeekQ.error(FAILURE);
    }

    /**
     * 获取电影详情
     * @param movie_id
     * @return
     */
    @GetMapping("/subject/{movie_id}")
    public ResultGeekQ<MoviePage> getDetail(@PathVariable Long movie_id){
        MoviePage moviePage = movieDetailService.getMovieDetail(movie_id);
        System.out.println(moviePage);

        if(moviePage != null){
            return ResultGeekQ.done(SUCCESS,moviePage);
        }
        return ResultGeekQ.error(FAILURE);
    }

    /**
     * 获取最多评论的电影列表
     * @return
     */
    @GetMapping("/mostComment")
    public ResultGeekQ<List<TopMovies>> getMostComment(){
        List<TopMovies> res = movieService.getMostComment();

        if(res == null){
            return ResultGeekQ.error(FAILURE);
        }
        return ResultGeekQ.done(SUCCESS,res);
    }

    /**
     * 获取最多收藏的电影列表
     * @return
     */
    @GetMapping("/mostCollected")
    public ResultGeekQ<List<TopMovies>> getMostCollected(){
        List<TopMovies> res = movieService.getMostCollected();

        if(res == null){
            return ResultGeekQ.error(FAILURE);
        }
        return ResultGeekQ.done(SUCCESS,res);
    }


    /**
     * 给 分类页面默认展示的最新电影，一页为 36 部，并且返回最大页数和当前页码
     * @return
     */
    @GetMapping("/typesNew/{curPage}")
    public ResultGeekQ<Map<String,Object>> getNew36(@PathVariable  int curPage){
        Map<String,Object> res = movieService.getNew36(curPage);
        if(res != null){
            return ResultGeekQ.done(SUCCESS,res);
        }
        return ResultGeekQ.error(FAILURE);
    }


    /**
     * 根据筛选信息筛选电影
     * @param map
     * @return
     */
    @PostMapping("/genres")
    public ResultGeekQ<Page<MovieDetail>> screen(@RequestBody  Map<String,Object> map){

        Page<MovieDetail> res = movieService.screen(map);

        return ResultGeekQ.done(FAILURE,res);
    }

    /**
     * 查找电影
     * @param msg
     * @return
     */
    @GetMapping("/search")
    public ResultGeekQ<Set<SearchMovie>> search(@RequestParam("msg") String msg){
        System.out.println(msg);

        Set<SearchMovie> res = movieService.searchMovie(msg);
        if(res != null){
            if(res.size() == 0){
                return ResultGeekQ.error(NOT_FOUND,null);
            }
            return ResultGeekQ.done(FOUND,res);
        }

        return ResultGeekQ.error(SEARCH_ERROR,null);
    }


    /**
     * 取消or收藏：
     */

    @PutMapping("/collect")
    public ResultGeekQ<Boolean> doCollect(@RequestBody Map<String,Object> map){
        Long movie_id = Long.parseLong(String.valueOf(map.get("movie_id")));
        Boolean isCollected = Boolean.valueOf(map.get("collected").toString());
        
        if(movieDetailService.doCollect(isCollected,movie_id) > 0){
            return ResultGeekQ.done(SUCCESS,true);
        }

        return ResultGeekQ.error(FAILURE,false);
    }

    @PutMapping("/score")
    public ResultGeekQ<Boolean> doScore(@RequestBody Map<String,Object> map){
        Long movie_id = Long.parseLong(String.valueOf(map.get("movie_id")));
        int score = Integer.parseInt(String.valueOf(map.get("score")));
        int oldScore = Integer.parseInt(String.valueOf(map.get("old_score")));

        if(movieDetailService.updateScore(movie_id,score,oldScore) > 0){
            ResultGeekQ.done(SUCCESS,true);
        }

        return ResultGeekQ.error(FAILURE,false);
    }

    @GetMapping("/viewCollect")
    public ResultGeekQ<List<MovieDetail>> viewCollections(){

        List<MovieDetail> res =  movieService.viewCollections();

        if(res != null){
            return ResultGeekQ.done(SUCCESS,res);
        }
        return ResultGeekQ.error(FAILURE,null);
    }

    @GetMapping("/viewHistroy")
    public ResultGeekQ<List<MovieDetail>> viewHistory(){
        List<MovieDetail> res =  movieService.viewHistory();

        if(res != null){
            return ResultGeekQ.done(SUCCESS,res);
        }
        return ResultGeekQ.error(FAILURE,null);
    }

    @GetMapping("/recommendation")
    public ResultGeekQ<List<TopMovies>> getRecommend(){
        List<TopMovies> res = movieService.getRecommendation();

        return  ResultGeekQ.done(SUCCESS,res);
    }
}



