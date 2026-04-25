package com.example.demo.services;

import com.example.demo.model.Booking;
import com.example.demo.request.booking.CreateBookingForCustomerRequest;
import com.example.demo.request.booking.CreateBookingRequest;
import com.example.demo.response.BookingResponseDTO;
import com.example.demo.response.PendingBookingDTO;

import java.util.List;

public interface BookingService {
    // Trả về DTO thay vì Entity
    BookingResponseDTO createBooking(CreateBookingRequest request);
    BookingResponseDTO createBookingForCustomer(CreateBookingForCustomerRequest request);

    BookingResponseDTO getBookingDTOById(Integer id);
    BookingResponseDTO getBookingDTOByCode(String bookingCode);
    List<BookingResponseDTO> getMyBookingsDTO();
    List<BookingResponseDTO> getAllBookingsDTO();
    
    Booking cancelBooking(Integer id);

    // Phương thức mới cho việc check-in
    BookingResponseDTO checkInBooking(String bookingCode);

    // Các phương thức mới cho luồng thanh toán VNPay
    PendingBookingDTO calculateBookingDetails(PendingBookingDTO pendingBooking);
    Booking createBookingFromPending(PendingBookingDTO pendingBooking);
}
