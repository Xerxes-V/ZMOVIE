package com.zz.zmovie.redis;

public class MoviePrefix extends BasePrefix {

    private static final int topExpire = 24*3600;

    private static final int movieDetailExpire = 3600;

    public static MoviePrefix topPrefix = new MoviePrefix("top",topExpire);     //榜单电影

    public static MoviePrefix getMovieDetail = new MoviePrefix("movieDetail",movieDetailExpire);        //电影详情

    public static MoviePrefix MovieData = new MoviePrefix("movieData",topExpire);

    public MoviePrefix(String prefix) {
        super(prefix);
    }

    public MoviePrefix(String prefix, int expireSeconds) {
        super(prefix, expireSeconds);
    }

}
