package com.zz.zmovie.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Likes {
    private Long id;
    private Long commentId;
    private Long userId;
    private int liked;
    private int type;
    private Date like_time;
}
