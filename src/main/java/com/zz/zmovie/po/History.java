package com.zz.zmovie.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class History {
    private Long id;
    private Long movieId;
    private Long userId;
    private Date browserTime;
}
