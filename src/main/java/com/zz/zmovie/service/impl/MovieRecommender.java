package com.zz.zmovie.service.impl;

/**
 * Created on 2023/12/19.
 *
 * @author WEZARD
 */
import java.util.Map;

public class MovieRecommender {
    private Map<Integer, Map<Integer, Double>> userRatings;

    public MovieRecommender(Map<Integer, Map<Integer, Double>> userRatings) {
        this.userRatings = userRatings;
    }

    public double calculatePearsonSimilarity(int user1, int user2) {
        Map<Integer, Double> ratings1 = userRatings.get(user1);
        Map<Integer, Double> ratings2 = userRatings.get(user2);

        // Calculate mean ratings
        double mean1 = calculateMean(ratings1);
        double mean2 = calculateMean(ratings2);

        // Calculate Pearson correlation coefficient
        double numerator = 0.0;
        double denominator1 = 0.0;
        double denominator2 = 0.0;

        for (int movieId : ratings1.keySet()) {
            if (ratings2.containsKey(movieId)) {
                double diff1 = ratings1.get(movieId) - mean1;
                double diff2 = ratings2.get(movieId) - mean2;

                numerator += diff1 * diff2;
                denominator1 += diff1 * diff1;
                denominator2 += diff2 * diff2;
            }
        }

        if (denominator1 == 0 || denominator2 == 0) {
            return 0.0; // Users have no common rated movies or perfect correlation
        }

        return numerator / Math.sqrt(denominator1 * denominator2);
    }

    private double calculateMean(Map<Integer, Double> ratings) {
        if (ratings.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        for (double rating : ratings.values()) {
            sum += rating;
        }

        return sum / ratings.size();
    }

    public static void main(String[] args) {
        Map<Integer, Map<Integer, Double>> userRatings = Map.of(
                1, Map.of(1, 4.0, 2, 5.0, 3, 3.0),
                2, Map.of(1, 4.0, 2, 5.0, 4, 3.0)
        );

        MovieRecommender recommender = new MovieRecommender(userRatings);
        int user1 = 1;
        int user2 = 2;

        double similarity = recommender.calculatePearsonSimilarity(user1, user2);

        System.out.println("Pearson Similarity between User " + user1 + " and User " + user2 + ": " + similarity);
    }
}
