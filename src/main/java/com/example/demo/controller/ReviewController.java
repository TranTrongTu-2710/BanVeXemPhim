package com.example.demo.controller;

import com.example.demo.model.Review;
import com.example.demo.request.review.CreateReviewRequest;
import com.example.demo.request.review.UpdateReviewRequest;
import com.example.demo.services.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // --- Public Routes ---

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Review>> getReviewsByMovie(@PathVariable Integer movieId) {
        return ResponseEntity.ok(reviewService.getReviewsByMovie(movieId));
    }

    // --- Authenticated Routes ---

    @PostMapping
    public ResponseEntity<Review> createReview(@Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(request));
    }

    @GetMapping("/my-reviews")
    public ResponseEntity<List<Review>> getMyReviews() {
        return ResponseEntity.ok(reviewService.getMyReviews());
    }

    // --- Admin Routes ---

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Review> approveReview(@PathVariable Integer id, @Valid @RequestBody UpdateReviewRequest request) {
        return ResponseEntity.ok(reviewService.approveReview(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Integer id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
