package com.example.demo.services;

import com.example.demo.model.Review;
import com.example.demo.request.review.CreateReviewRequest;
import com.example.demo.request.review.UpdateReviewRequest;

import java.util.List;

public interface ReviewService {
    Review createReview(CreateReviewRequest request);
    Review getReviewById(Integer id);
    List<Review> getReviewsByMovie(Integer movieId);
    List<Review> getMyReviews();
    Review approveReview(Integer id, UpdateReviewRequest request);
    void deleteReview(Integer id);
}
