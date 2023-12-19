package com.zz.zmovie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zz.zmovie.config.UserThreadLocal;
import com.zz.zmovie.mapper.MovieDetailMapper;
import com.zz.zmovie.mapper.RecommendationMapper;
import com.zz.zmovie.mapper.UserScoredMapper;
import com.zz.zmovie.po.Recommendation;
import com.zz.zmovie.po.User;
import com.zz.zmovie.po.UserScored;
import com.zz.zmovie.redis.RedisService;
import com.zz.zmovie.redis.UserPrefix;
import com.zz.zmovie.service.RecommendationService;
import com.zz.zmovie.test.test;
import com.zz.zmovie.vo.TopMovies;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.JDBCDataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
public class RecommendationServiceImpl implements RecommendationService {
    private final static int NEIGHBORHOOD_NUM = 3;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MovieDetailMapper movieDetailMapper;

    @Autowired
    private RecommendationMapper recommendationMapper;

    @Autowired
    private UserScoredMapper userScoredMapper;






    // 计算皮尔逊相关系数
    private double calculatePearsonSimilarity(int user1, int user2,Map<Integer, Map<Integer, Double>> userRatings) {
        Map<Integer, Double> ratings1 = userRatings.get(user1);
        Map<Integer, Double> ratings2 = userRatings.get(user2);

        int n = 0;
        double sum1 = 0.0, sum2 = 0.0, sum1Sq = 0.0, sum2Sq = 0.0, pSum = 0.0;

        for (int movieId : ratings1.keySet()) {
            if (ratings2.containsKey(movieId)) {
                n++;
                double rating1 = ratings1.get(movieId);
                double rating2 = ratings2.get(movieId);

                sum1 += rating1;
                sum2 += rating2;
                sum1Sq += Math.pow(rating1, 2);
                sum2Sq += Math.pow(rating2, 2);
                pSum += rating1 * rating2;
            }
        }

        if (n == 0) {
            return 0;
        }

        // 计算皮尔逊相关系数
        double num = pSum - (sum1 * sum2 / n);
        double den = Math.sqrt((sum1Sq - Math.pow(sum1, 2) / n) * (sum2Sq - Math.pow(sum2, 2) / n));

        if (den == 0) {
            return 0;
        }

        return num / den;
    }

    // 针对指定用户，找到相似用户并推荐电影
//    评分数据（用户ID，（电影ID，评分））
    public Map<Integer, Double> recommendMovies(int targetUser, Map<Integer, Map<Integer, Double>> userRatings) {
        Map<Integer, Double> recommendations = new HashMap<>();
        for (int user : userRatings.keySet()) {
            if (user != targetUser) {
                double similarity = calculatePearsonSimilarity(targetUser, user,userRatings);
                System.out.println(similarity);
                if (similarity > 0) {
                    Map<Integer, Double> userRatingsRes = userRatings.get(user);
                    for (int movieId : userRatingsRes.keySet()) {
                        if (!userRatingsRes.get(movieId).isNaN() && !userRatings.get(targetUser).containsKey(movieId)) {
                            double rating = userRatingsRes.get(movieId) * similarity;
                            recommendations.merge(movieId, rating, Double::sum);
                        }
                    }
                }
            }
        }
        return recommendations;
    }

    @Test
    public void  test(){
        int targetUser = 1;
        // 初始化用
        Map<Integer, Map<Integer, Double>>  userRatings = new HashMap<>();
        // 仅为示例，实际应从数据库中读取数据
        userRatings.put(1, Map.of(1, 5.0, 2, 4.0, 3, 3.0));
        userRatings.put(2, Map.of(1, 5.0, 2, 3.0, 4, 4.0));
        userRatings.put(3, Map.of(2, 4.0, 3, 5.0, 4, 2.0));
        Map<Integer, Double> recommendations = recommendMovies(targetUser,userRatings);

        System.out.println("Recommended movies for User " + targetUser + ":");
        for (int movieId : recommendations.keySet()) {
            System.out.println("Movie ID: " + movieId + ", Predicted Rating: " + recommendations.get(movieId));
        }
    }

    @Override
    public void getRecommends(Long userId)  {
        User user = UserThreadLocal.get();

    }









    @Override
    public void getRecommendsMaHout(Long userId)  {
            User user = UserThreadLocal.get();
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
                try {
                    //生成皮尔逊相关系数列表
                    ItemSimilarity itemSimilarity = new PearsonCorrelationSimilarity(model);
                    //计算相似得分
                    Recommender recommender = new GenericItemBasedRecommender(model, itemSimilarity);
                    System.out.println("开始进行推荐计算");
                    //根据用户id获取，获取前10部
                    recommendations = recommender.recommend(114, 10);
                    System.out.println("done");
                    for (RecommendedItem recommendation : recommendations) {
                        System.out.println(recommendation);
                    }

                    for (int i = 0; i < recommendations.size(); i++) {
                        Recommendation recommendation = new Recommendation();
                        recommendation.setMovieId(recommendations.get(i).getItemID());

                        QueryWrapper<Recommendation> queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq("user_id",userId);
                        queryWrapper.eq("rank",i);

                        recommendationMapper.update(recommendation,queryWrapper);
                    }
                } catch (TasteException e) {
                    e.printStackTrace();
                }

    }

    //计算皮尔逊相关系数
    public Double getRelate(List<Integer> xs, List<Integer>  ys){
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

    public void doRecommend(){
        QueryWrapper<UserScored> userScoredQueryWrapper = new QueryWrapper<>();
        List<UserScored>  allUserScore = userScoredMapper.selectList(userScoredQueryWrapper);

        userScoredQueryWrapper.eq("user_id",114l);
        List<UserScored> userScores = userScoredMapper.selectList(userScoredQueryWrapper);

        Long userId = allUserScore.get(0).getUserId();
        List<Long> movieIds = new ArrayList<>();
        for (int i = 0; i < allUserScore.size(); i++) {
            if(userId == allUserScore.get(i).getUserId() ){
//                movieIds.add(allUserScore.get(i).getScore());
            }else{
                movieIds = new ArrayList<>();
            }
//            getRelate();
        }

    }











    // 基于内容的推荐算法
    public List<Long> itemBasedRecommender(long userID, int size)  throws TasteException {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setServerName("localhost");
        dataSource.setUser("root");
        dataSource.setPassword("123456");
        dataSource.setDatabaseName("zmovie");


        //获取数据模型
        JDBCDataModel dataModel = new MySQLJDBCDataModel(dataSource, "user_scored", "user_id", "movie_id", "score","score_time");

        DataModel model = dataModel;


        List<RecommendedItem> recommendations = null;
            ItemSimilarity itemSimilarity = new PearsonCorrelationSimilarity(model);
            Recommender recommender = new GenericItemBasedRecommender(model, itemSimilarity);
            System.out.println(dataModel);
            recommendations = recommender.recommend(userID, size);
            System.out.println(recommendations);


        return getRecommendedItemIDs(recommendations);
    }


    private List<Long> getRecommendedItemIDs(List<RecommendedItem> recommendations){
        List<Long> recommendItems = new ArrayList<>();
        for(int i = 0 ; i < recommendations.size() ; i++) {
            RecommendedItem recommendedItem=recommendations.get(i);
            recommendItems.add(recommendedItem.getItemID());
        }
        return recommendItems;
    }

    public List<Long> userBasedRecommender(long userID,int size) throws TasteException {

        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setServerName("localhost");
        dataSource.setUser("root");
        dataSource.setPassword("123456");
        dataSource.setDatabaseName("zmovie");


        //获取数据模型
        JDBCDataModel dataModel = new MySQLJDBCDataModel(dataSource, "user_scored", "user_id", "movie_id", "score","score_time");

        DataModel model = dataModel;

        UserSimilarity similarity  = new EuclideanDistanceSimilarity(model );
        NearestNUserNeighborhood neighbor = new NearestNUserNeighborhood(NEIGHBORHOOD_NUM, similarity, model );
        Recommender recommender = new CachingRecommender(new GenericUserBasedRecommender(model , neighbor, similarity));
        List<RecommendedItem> recommendations = recommender.recommend(userID, size);
        return getRecommendedItemIDs(recommendations);
    }


}
