package com.zz.zmovie.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    private Long id;
    private Long movieId;
    private int commentNum;
    private double score;
    private int ratingNum;
    private int collectNum;
    private Date putOnDate;
    private int dataId;

}
