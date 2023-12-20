package com.zz.zmovie.redis;

public class AdminPrefix  extends BasePrefix {

    private static final int  adminExpire = 3600;

    public AdminPrefix(String prefix) {
        super(prefix);
    }

    public static final AdminPrefix adminUserMsg = new AdminPrefix("adminUserMsg",adminExpire);

    public static final AdminPrefix adminMovieMsg = new AdminPrefix("adminMovieMsg",adminExpire);


    public AdminPrefix(String prefix,int adminExpire) {
        super(prefix,adminExpire);
    }
}
