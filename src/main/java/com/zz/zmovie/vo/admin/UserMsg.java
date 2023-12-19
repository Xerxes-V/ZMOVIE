package com.zz.zmovie.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserMsg {

    private int userNum;
    private int movieCommentNum;
    private int commentNum;
    private int banUser;

    private List<Integer> movieCommentNumChange;
    private List<Integer> commentNumChange;
}
