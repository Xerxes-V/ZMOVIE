package com.zz.zmovie.test;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zz.zmovie.exception.GlobleException;
import com.zz.zmovie.mapper.MovieMapper;
import com.zz.zmovie.po.User;
import com.zz.zmovie.redis.MoviePrefix;
import com.zz.zmovie.redis.RedisService;
import com.zz.zmovie.utils.ResultStatus;
import com.zz.zmovie.vo.TopMovies;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.JDBCDataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.IntStream;

public class test {

    private static ThreadLocal<String> threadLocal = new ThreadLocal<>();

    @Autowired
    private RedisService redisService;

    @Test
    public void test(){
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);

        List<User> l = new ArrayList<>();
        User u1 = new User();
        u1.setUsername("u1");
        u1.setId(1l);
        User u2 = new User();
        u2.setId(2l);
        u2.setUsername("u2");

        l.add(u1);
        l.add(u2);

        for (int i = 0; i < l.size(); i++) {
            System.out.println(l.get(i).toString());
        }

//        JSONArray jsonArray = JSONUtil.parseArray(l);
//
//        System.out.println(jsonArray.toString());
//
//        String str = jsonArray.toString();
//
//        JSONArray jsonArray1 = JSONUtil.parseArray(str);
//        System.out.println(jsonArray1);
//
//        List<User> us = JSONUtil.toList(jsonArray,User.class);
//        us.forEach(System.out::println);

        redisService.setList(MoviePrefix.topPrefix,"list",l);
    }


    //1.26
//    public static void main(String[] args) throws Exception {
//        MysqlDataSource dataSource = new MysqlDataSource();
//        dataSource.setServerName("localhost");
//        dataSource.setUser("root");
//        dataSource.setPassword("123456");
//        dataSource.setDatabaseName("zmovie");
//
//        JDBCDataModel dataModel = new MySQLJDBCDataModel(dataSource, "ratings", "userId", "movieId", "rating", "timestamp");
//
//        DataModel model = dataModel;
//        UserSimilarity similarity=new PearsonCorrelationSimilarity(model);
//        UserNeighborhood neighborhood=new NearestNUserNeighborhood(2,similarity,model);
//
//        Recommender recommender=new GenericUserBasedRecommender(model,neighborhood,similarity);
//
//        List<RecommendedItem> recommendations = recommender.recommend(2, 3);
//        for (RecommendedItem recommendation : recommendations) {
//            System.out.println(recommendation);
//        }
//    }



    @Test
    public void  test1()  throws Exception{

//        数据库中获取数据
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setServerName("localhost");
        dataSource.setUser("root");
        dataSource.setPassword("123456");
        dataSource.setDatabaseName("zmovie");

        JDBCDataModel dataModel = new MySQLJDBCDataModel(dataSource, "ratings", "userId", "movieId", "rating", "score_time");
//        JDBCDataModel dataModel = new MySQLJDBCDataModel(dataSource, "user_scored", "user_id", "movie_id", "score", "score_time");
    //转换为数据模型
        DataModel model = dataModel;

        List<RecommendedItem> recommendations = null;
        //生成皮尔逊相关系数列表
        ItemSimilarity itemSimilarity = new PearsonCorrelationSimilarity(model);
        //计算相似得分
        System.out.println("here");
        Recommender recommender = new GenericItemBasedRecommender(model, itemSimilarity);
        //根据用户id获取，获取前10部
        recommendations = recommender.recommend(114, 10);
        for (RecommendedItem recommendation : recommendations) {
            System.out.println(recommendation);
        }
        System.out.println("end");
    }
    
    @Test
    public void test2() throws  Exception{

        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setServerName("localhost");
        dataSource.setUser("root");
        dataSource.setPassword("123456");
        dataSource.setDatabaseName("zmovie");

        JDBCDataModel dataModel = new MySQLJDBCDataModel(dataSource, "test", "user_id", "movie_id", "score", "score_time");

        DataModel model = dataModel;


        UserSimilarity similarity=new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood=new NearestNUserNeighborhood(2,similarity,model);

        Recommender recommender=new GenericUserBasedRecommender(model,neighborhood,similarity);

        List<RecommendedItem> recommendations = recommender.recommend(2, 3);
        for (RecommendedItem recommendation : recommendations) {
            System.out.println(recommendation);
        }
    }

    public static Double getRelate(List<Integer> xs, List<Integer>  ys){
        int n=xs.size();
        double Ex= xs.stream().mapToDouble(x->x).sum();
        double Ey=ys.stream().mapToDouble(y->y).sum();
        double Ex2=xs.stream().mapToDouble(x->Math.pow(x,2)).sum();
        double Ey2=ys.stream().mapToDouble(y->Math.pow(y,2)).sum();

        double Exy= IntStream.range(0,n).mapToDouble(i->xs.get(i)*ys.get(i)).sum();

        double numerator=Exy-Ex*Ey/n;

        double denominator=Math.sqrt((Ex2-Math.pow(Ex,2)/n)*(Ey2-Math.pow(Ey,2)/n));
        if (denominator==0) return 0.0;
        return numerator/denominator;
    }

    
    @Test
    public void test4(){
        Random random = new Random();
//        System.out.println(random.nextInt(20));;


        List<Integer> movies = new ArrayList<>();
        movies.add(1);

        List<Integer> movies2 = new ArrayList<>();
        movies2.add(3);
        movies.addAll(movies2);

        List<Integer> movies3 = new ArrayList<>();
        movies3.add(35);

        movies.addAll(movies3);


//        System.out.println(movies.size());
//        for (int m:
//             movies) {
//            System.out.println(m);
//        }

        Set<Integer> indexs = new HashSet<>();
        while(indexs.size() < 10){
            indexs.add(random.nextInt(22));
        }

        for (int i : indexs) {
//          syso  res.add(movies.get(i));
            System.out.println(i);
            
        }
    }
}
