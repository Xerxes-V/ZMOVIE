package com.zz.zmovie.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchMovie {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
    private Integer releaseDate;
    private String genres;
    private String staring;
    private String post;


    private Double score;
    private int collectNum;
    private int ratingNum;
    private int commentNum;
}
