package com.zz.zmovie.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieMsg {
    private int movieNum;
    private int increaseNum;
    private String hottestMovie;

        private List<Integer> movieAreaNum;
    private List<Integer> movieMakeOfByStars;       //各个分段数量
}
