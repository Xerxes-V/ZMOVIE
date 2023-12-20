package com.zz.zmovie.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyMovieComments {


    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String content;
    private Date commentTime;

    private String name;
    private String post;



    private int score;


    private Long movieId;


}
