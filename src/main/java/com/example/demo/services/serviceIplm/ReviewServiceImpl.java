package com.example.demo.services.serviceIplm;

import com.example.demo.model.Booking;
import com.example.demo.model.Movie;
import com.example.demo.model.Review;
import com.example.demo.model.User;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.request.review.CreateReviewRequest;
import com.example.demo.request.review.UpdateReviewRequest;
import com.example.demo.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    public Review createReview(CreateReviewRequest request) {
        User currentUser = getCurrentUser();
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found."));

        if (reviewRepository.existsByUserIdAndMovieId(currentUser.getId(), movie.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already reviewed this movie.");
        }

        Booking booking = null;
        if (request.getBookingId() != null) {
            booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
            // Optional: Check if the booking belongs to the current user and is for the correct movie
        }

        Review review = Review.builder()
                .user(currentUser)
                .movie(movie)
                .booking(booking)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        return reviewRepository.save(review);
    }

    @Override
    public Review getReviewById(Integer id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found."));
    }

    @Override
    public List<Review> getReviewsByMovie(Integer movieId) {
        return reviewRepository.findByMovieId(movieId);
    }

    @Override
    public List<Review> getMyReviews() {
        User currentUser = getCurrentUser();
        return reviewRepository.findByUserId(currentUser.getId());
    }

    @Override
    public Review approveReview(Integer id, UpdateReviewRequest request) {
        Review review = getReviewById(id);
        if (request.getIsApproved() != null) {
            review.setIsApproved(request.getIsApproved());
        }
        return reviewRepository.save(review);
    }

    @Override
    public void deleteReview(Integer id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found.");
        }
        reviewRepository.deleteById(id);
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated.");
    }
}
