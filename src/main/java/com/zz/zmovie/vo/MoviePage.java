package com.zz.zmovie.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//展示电影详情

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoviePage {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
    private String englishName;
    private String director;
    private String writer;
    private String staring;
    private String genres;
    private String country;
    private String language;
    private int releaseDate;
    private int length;
    private String summary;
    private String post;
    private int doubanDataId;

    private double score;
    private int myScore;
    private int ratingNum;
    private boolean isCollected;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;


}
