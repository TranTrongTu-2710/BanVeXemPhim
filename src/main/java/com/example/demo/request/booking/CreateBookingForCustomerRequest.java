package com.example.demo.request.booking;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreateBookingForCustomerRequest extends CreateBookingRequest {

    private Integer userId;
}
