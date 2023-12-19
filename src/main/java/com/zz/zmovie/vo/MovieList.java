package com.zz.zmovie.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieList {
    private Long id;
    private String name;
    private String post;
    private Double score;
}
