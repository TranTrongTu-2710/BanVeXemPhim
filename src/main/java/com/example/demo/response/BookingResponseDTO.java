package com.example.demo.response;

import com.example.demo.model.Booking;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class BookingResponseDTO {
    private Integer id;
    private String bookingCode;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private Booking.PaymentMethod paymentMethod;
    private Booking.PaymentStatus paymentStatus;
    private Booking.BookingStatus bookingStatus;
    private LocalDateTime bookingDate;
    private UserResponseDTO user;
    private ShowtimeResponseDTO showtime;
    private List<String> seats; // THAY ĐỔI: Từ List<BookingSeatResponseDTO> thành List<String>
    private List<BookingFoodResponseDTO> bookingFoods;

    public BookingResponseDTO(Booking booking) {
        this.id = booking.getId();
        this.bookingCode = booking.getBookingCode();
        this.totalAmount = booking.getTotalAmount();
        this.discountAmount = booking.getDiscountAmount();
        this.finalAmount = booking.getFinalAmount();
        this.paymentMethod = booking.getPaymentMethod();
        this.paymentStatus = booking.getPaymentStatus();
        this.bookingStatus = booking.getBookingStatus();
        this.bookingDate = booking.getBookingDate();

        if (booking.getUser() != null) {
            this.user = new UserResponseDTO(booking.getUser());
        }
        if (booking.getShowtime() != null) {
            this.showtime = new ShowtimeResponseDTO(booking.getShowtime());
        }
        
        // THAY ĐỔI: Logic để lấy danh sách tên ghế
        if (booking.getBookingSeats() != null) {
            this.seats = booking.getBookingSeats().stream()
                    .map(bookingSeat -> bookingSeat.getSeat().getRowName() + bookingSeat.getSeat().getSeatNumber())
                    .collect(Collectors.toList());
        }

        if (booking.getBookingFoods() != null) {
            this.bookingFoods = booking.getBookingFoods().stream()
                    .map(BookingFoodResponseDTO::new)
                    .collect(Collectors.toList());
        }
    }
}
