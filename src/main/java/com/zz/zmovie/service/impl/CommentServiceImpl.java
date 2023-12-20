package com.zz.zmovie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zz.zmovie.mapper.*;
import com.zz.zmovie.service.CommentService;
import com.zz.zmovie.config.UserThreadLocal;
import com.zz.zmovie.po.*;
import com.zz.zmovie.vo.AllComments;
import com.zz.zmovie.vo.MyComments;
import com.zz.zmovie.vo.MyMovieComments;
import com.zz.zmovie.vo.SonsComments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl implements CommentService {
    
    @Autowired
    private CommentsMapper commentsMapper;

    @Autowired
    private MovieCommentsMapper movieCommentsMapper;

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private ReportCommentMapper reportCommentMapper;

    @Autowired
    private LikesMapper likesMapper;

    /**
     * 根据电影id找出所有的评论信息
     * @param movie_id
     * @return
     */
    @Override
    public List<AllComments> getAllComments(Long movie_id,int start) {
        User user = UserThreadLocal.get();

        //先根据电影 id 查找出影评：
        List<AllComments> list = commentsMapper.getAllMovieComments(movie_id,start*6);            //这种方式是值获取前 30 条评论

////        不如全部获取，这样不用多次加载
//        List<AllComments> list = commentsMapper.getAllMovieComments(movie_id,start*6);
        //根据影评 id 获取出子评论，放在各自 sons 中
        for(AllComments a : list){
            a.setSons(commentsMapper.getSons(a.getId()));
//            QueryWrapper<Likes> queryWrapper = new QueryWrapper<>();
//            queryWrapper.eq("user_id",user.getId());
//            queryWrapper.eq("comment_id",a.getId());
//            queryWrapper.eq("type",1);
//            a.setMyLike(likesMapper.selectOne(queryWrapper).getLike());
        }

        return list;
    }

    /**
     * 发布影评
     * @param map
     * @return
     */
    @Override
    public AllComments publishMovieComment(Map<String, Object> map) {
        User user = UserThreadLocal.get();

        MovieComments movieComments = new MovieComments();
        movieComments.setMovieId(Long.parseLong(map.get("movieId").toString()));
        movieComments.setContent(map.get("content").toString());
        movieComments.setUserId(user.getId());
        movieComments.setCommentTime(new Date());

        movieCommentsMapper.insert(movieComments);

        Long ID=movieComments.getId();
        movieComments.setId(ID);


        //返回给前端显示
        AllComments allComments = new AllComments();
        allComments.setId(ID);
        allComments.setUserName(user.getUsername());
        allComments.setAvatar(user.getAvatar());
        allComments.setCommentTime(new Date());
        allComments.setContent(map.get("content").toString());
        allComments.setMovieId(Long.parseLong(map.get("movieId").toString()));

        return allComments;
    }

    /**
     * 发布评论回复
     * @param map
     * @return
     */
    @Override
    public SonsComments reply(Map<String, Object> map) {
        User user = UserThreadLocal.get();

        Comments comments = new Comments();
        comments.setUserId(user.getId());
        comments.setCommentTime(new Date());
        comments.setContent(map.get("content").toString());
        comments.setMcId(Long.parseLong(map.get("mcId").toString()));
        comments.setMovieId(Long.parseLong(map.get("movieId").toString()));
        comments.setParentId(Long.parseLong(map.get("parentId").toString()));
        comments.setTargetUserName(map.get("targetUserName").toString());

        commentsMapper.insert(comments);

        Long ID= comments.getId();
        comments.setId(ID);
        //返回到前端的回复：
        SonsComments sonsComments = new SonsComments();
        sonsComments.setId(ID);
        sonsComments.setCommentTime(new Date());
        sonsComments.setContent(map.get("content").toString());
        sonsComments.setMcId(Long.parseLong(map.get("mcId").toString()));
        sonsComments.setMovieId(Long.parseLong(map.get("movieId").toString()));
        sonsComments.setParentId(Long.parseLong(map.get("parentId").toString()));
        sonsComments.setTargetUserName(map.get("targetUserName").toString());
        sonsComments.setUsername(user.getUsername());


        return sonsComments;
    }

    /**
     * 删除影评
     * @param movie_id
     * @return
     */
    @Override
    public int deleteMovieComment(Long comment_id) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("mc_id",comment_id);
        commentsMapper.delete(queryWrapper);
        return  movieCommentsMapper.deleteById(comment_id);
    }

    /**
     * 删除回复
     * @param reply_id
     * @return
     */
    @Override
    public int deleteReply(Long reply_id) {
        return commentsMapper.deleteById(reply_id);
    }


    /**
     * 展示个人所有的影评
     * @return
     */
    @Override
    public List<MyMovieComments> getPersonalMovieComments() {
        User user = UserThreadLocal.get();

        List<MyMovieComments> res =  movieCommentsMapper.getMyComments(user.getId());

        res.forEach(System.out::println);

        return res;
    }



    /**
     * 展示个人所有的评论
     * @return
     */
    @Override
    public List<MyComments> viewPersonalComments() {
        User user = UserThreadLocal.get();


        List<MyComments> list = commentsMapper.getPersonalComments(user.getId());

        return list;
    }

    /**
     * 对不良言论进行举报
     * @param comment_id
     * @param type
     * @return
     */

    @Override
    public int report(Long comment_id, int type,String message) {
        User user = UserThreadLocal.get();

        int res = 0;

        Report report = new Report();
        report.setMessage(message);
        report.setReportTime(new Date());
        report.setCommentId(comment_id);
        report.setType(type);
        res = reportMapper.insert(report);

        return res;
    }

    /**
     * 赞or踩
     * @param comment_id
     * @param type
     * @param like
     * @return
     */
    @Override
    public int like(Long comment_id, int type, int like,int past) {
        User user = UserThreadLocal.get();

        //首先更新个人记录表
        //取消点赞/踩：
        QueryWrapper<Likes> queryWrapper = new QueryWrapper();
        queryWrapper.eq("comment_id",comment_id);
        queryWrapper.eq("type",type);
        queryWrapper.eq("user_id",user.getId());
        if(like == 0){
            likesMapper.delete(queryWrapper);
        }else{
            Likes likes = new Likes();
            likes.setCommentId(comment_id);
            likes.setLiked(like);
            likes.setType(type);
            likes.setUserId(user.getId());
            likes.setLike_time(new Date());

            //踩得话删掉赞的记录，反之
            likesMapper.delete(queryWrapper);

            likesMapper.insert(likes);
        }


        like = like - past;

        //更新评论表
        if(type == 1){      //影评
            movieCommentsMapper.updateLikes(comment_id,like );
        }else{
            commentsMapper.updateLikes(comment_id,like);
        }

        return 1;
    }
}
