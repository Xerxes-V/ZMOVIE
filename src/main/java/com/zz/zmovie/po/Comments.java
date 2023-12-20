package com.zz.zmovie.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comments {
    private Long id;
    private Long movieId;
    private Long mcId;
    private Long parentId;
    private Long userId;
    private String content;
    private Date commentTime;
    private String targetUserName;
    private Integer likes;
    private Integer dislikes;

}
