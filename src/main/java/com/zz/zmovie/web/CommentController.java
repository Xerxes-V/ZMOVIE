package com.zz.zmovie.web;

import com.zz.zmovie.service.CommentService;
import com.zz.zmovie.utils.ResultGeekQ;
import com.zz.zmovie.vo.AllComments;
import com.zz.zmovie.vo.MyComments;
import com.zz.zmovie.vo.MyMovieComments;
import com.zz.zmovie.vo.SonsComments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.zz.zmovie.utils.ResultStatus.FAILURE;
import static com.zz.zmovie.utils.ResultStatus.SUCCESS;

@RestController
@CrossOrigin
@RequestMapping("/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;

    /**
     *初始化数据，根据电影id 
     *
     * @param id
     * @return
     */
    @GetMapping("/init/{id}")
    public ResultGeekQ<List<AllComments>> getAllComments(@PathVariable Long id){
        List<AllComments> res = commentService.getAllComments(id,0);
//        res.forEach(System.out::println);
        if(res == null){
            return ResultGeekQ.error(FAILURE);
        }
        return ResultGeekQ.done(SUCCESS,res);
    }

    /**
     * 发布影评
     * @param map
     * @return
     */
    @PutMapping("/publismc")
    public ResultGeekQ<AllComments> publishMovieComment(@RequestBody Map<String,Object> map){
        AllComments res = commentService.publishMovieComment(map);
        if(res != null){
            return ResultGeekQ.done(SUCCESS,res);
        }else{
            return ResultGeekQ.error(FAILURE,null);
        }

    }

    /**
     * 发布评论回复
     * @param map
     * @return
     */
    @PutMapping("/publisreply")
    public  ResultGeekQ<SonsComments> publishReply(@RequestBody Map<String,Object> map){
//        System.out.println(map.get("movieId"));
//        System.out.println(map.get("content"));
//        System.out.println(map.get("targetUserName"));
//        System.out.println(map.get("mcId"));
//        System.out.println(map.get("parentId"));
        SonsComments res = commentService.reply(map);

        if( res != null) {
            return ResultGeekQ.done(SUCCESS, res);
        }
            return ResultGeekQ.error(FAILURE,null);
    }

    /**
     * 删除影评：
     * @param movie_id
     * @return
     */
    @DeleteMapping("/deleteComment/{comment_id}")
    public ResultGeekQ<Boolean> deleteMovieComment(@PathVariable Long comment_id){
        if(commentService.deleteMovieComment(comment_id)>0){
            return ResultGeekQ.done(SUCCESS,true);
        }
        return ResultGeekQ.error(FAILURE,false);
    }


    /**
     * 删除影评回复
     * @param reply_id
     * @return
     */
    @DeleteMapping("/deleteReply/{reply_id}")
    public ResultGeekQ<Boolean> deleteReply( @PathVariable Long reply_id){
        if(commentService.deleteReply(reply_id)>0){
            return ResultGeekQ.done(SUCCESS,true);
        }
        return ResultGeekQ.error(FAILURE,false);
    }

    /**
     * 展示更多的评论消息
     * @param movie_id
     * @param curPage
     * @return
     */
    @GetMapping("/showMoreComments/{movie_id}/{curPage}")
    public ResultGeekQ<List<AllComments>> showMore(@PathVariable Long movie_id,@PathVariable int curPage){
        List<AllComments> list =  commentService.getAllComments(movie_id,curPage);

        if(list == null){
            return ResultGeekQ.error(FAILURE,null);
        }else{
            return ResultGeekQ.done(SUCCESS,list);
        }
    }

    /**
     * 展示个人所有的影评
     * @return
     */
    @GetMapping("/viewmc")
    public ResultGeekQ<List<MyMovieComments>> viewPersonalMovieComments(){
        List<MyMovieComments> res = commentService.getPersonalMovieComments();

        if(res != null){
            return  ResultGeekQ.done(SUCCESS,res);
        }

        return ResultGeekQ.done(SUCCESS,null);
    }

    /**
     * 展示个人所有的评论
     * @return
     */
    @GetMapping("/viewComments")
    public ResultGeekQ<List<MyComments>> ViewComments(){
        List<MyComments> res = commentService.viewPersonalComments();
        if(res != null){
            return ResultGeekQ.done(SUCCESS,res);
        }

        return ResultGeekQ.error(FAILURE,null);
    }

    /**
     * 对不良言论进行举报
     * @param map
     * @return
     */
    @PostMapping("/report")
    public ResultGeekQ<Boolean> report(@RequestBody Map<String,Object> map){
        System.out.println(String.valueOf(map.get("message")));
        int res =  commentService.report(Long.parseLong(String.valueOf(map.get("comment_id"))),Integer.parseInt(String.valueOf(map.get("type"))),String.valueOf(map.get("message")));

        if(res > 0){
            return ResultGeekQ.done(SUCCESS,true);
        }else{
            return ResultGeekQ.error(FAILURE,false);
        }
    }


    @PostMapping("/like")
    public ResultGeekQ<Boolean> likeOrDislike(@RequestBody Map<String,Object> map){
        Long commentId = Long.parseLong(String.valueOf(map.get("comment_id")));
        int type = Integer.parseInt(String.valueOf(map.get("type")));
        int like = Integer.parseInt(String.valueOf(map.get("like")));
        int past = Integer.parseInt(String.valueOf(map.get("past")));


        commentService.like(commentId,type,like,past);

        return ResultGeekQ.error(FAILURE,false);
    }
}
