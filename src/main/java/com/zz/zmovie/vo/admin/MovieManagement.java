package com.zz.zmovie.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieManagement {
    private Long id;
    private String name;
    private String director;
    private String staring;
    private String country;
    private int releaseDate;
}
