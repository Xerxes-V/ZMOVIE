package com.zz.zmovie.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadMovie {
    private String name;
    private String director;
    private String writer;
    private List<String> staring;
    private List<String> genres;
    private Date releaseDate;
    private String language;
    private String summary;
    private int length;
    private String englishName;
    private String area;

    private MultipartFile post;
    private String path;
}
