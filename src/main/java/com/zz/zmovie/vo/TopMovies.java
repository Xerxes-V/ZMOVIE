package com.zz.zmovie.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//展示多个电影

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopMovies {
    private Long id;
    private String name;
    private Integer releaseDate;
    private String genres;
    private String staring;
    private String post;
    private Double score;

}
