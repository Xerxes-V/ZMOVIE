package com.zz.zmovie.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Recommendation {
    private Long id;
    private Long movieId;
    private Long userId;
    private int rank;

}
