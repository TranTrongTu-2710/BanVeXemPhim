package com.example.demo.request.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReviewRequest {
    @NotNull
    private Integer movieId;

    private Integer bookingId; // Optional

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    private String comment;
}
