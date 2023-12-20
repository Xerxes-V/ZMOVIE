package com.zz.zmovie.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserScored {
    private Long id;
    private Long userId;
    private Long movieId;
    private int score;
    private Integer movieDataId;

    private Date scoreTime;

}
