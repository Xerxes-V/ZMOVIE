package com.zz.zmovie.po;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieDetail {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
    private String englishName;
    private String director;
    private String writer;
    private String staring;
    private String genres;
    private String country;
    private String language;
    private int releaseDate;
    private int length;
    private String summary;
    private String post;
    private int doubanDataId;

}
