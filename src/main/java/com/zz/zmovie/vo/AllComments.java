package com.zz.zmovie.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.zz.zmovie.po.Comments;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

//展示影评和评论

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllComments {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long movieId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String content;
    private int likes;
    private int commentsNum;
    private Date commentTime;
    private int dislikes;

    private int myLike;

    private String avatar;
    private String userName;
    private int score;

    private  boolean showReply= false;
    private int showReplyNums = 2; //最多展示多少回复
    private int useless = -1;       //没有用的，用于同时和视图改变

    //一个影评带一系列评论:
    private List<SonsComments> sons;

}
