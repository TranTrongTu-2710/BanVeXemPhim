package com.example.demo.controller;

import com.example.demo.request.booking.CheckInRequest;
import com.example.demo.request.booking.CreateBookingForCustomerRequest;
import com.example.demo.request.booking.CreateBookingRequest;
import com.example.demo.response.BookingResponseDTO;
import com.example.demo.services.BookingService;
import com.example.demo.services.QrCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final QrCodeService qrCodeService; // Inject QrCodeService

    // === Public & Authenticated Routes ===

    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingResponseDTO>> getMyBookings() {
        return ResponseEntity.ok(bookingService.getMyBookingsDTO());
    }

    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getBookingById(@PathVariable Integer id) {
        return ResponseEntity.ok(bookingService.getBookingDTOById(id));
    }

    /**
     * Endpoint để tạo và trả về hình ảnh mã QR cho một booking.
     * @param bookingCode Mã của booking cần tạo QR.
     * @return Hình ảnh PNG của mã QR.
     */
    @GetMapping(value = "/qr/{bookingCode}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCodeForBooking(@PathVariable String bookingCode) {
        try {
            // Kiểm tra xem booking có tồn tại không để tránh tạo QR cho mã không hợp lệ
            bookingService.getBookingDTOByCode(bookingCode); 
            
            // Dữ liệu được mã hóa trong QR chính là bookingCode
            byte[] qrCodeImage = qrCodeService.generateQrCodeImage(bookingCode, 250, 250);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrCodeImage);
        } catch (Exception e) {
            // Có thể trả về một ảnh lỗi mặc định hoặc một mã lỗi HTTP
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Map<String, String>> cancelBooking(@PathVariable Integer id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok(Map.of("message", "Booking cancelled successfully."));
    }

    // === Staff & Admin Routes ===

    @PostMapping("/staff/create")
    public ResponseEntity<BookingResponseDTO> createBookingForCustomer(@Valid @RequestBody CreateBookingForCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBookingForCustomer(request));
    }

    @PostMapping("/check-in")
    public ResponseEntity<BookingResponseDTO> checkInBooking(@Valid @RequestBody CheckInRequest request) {
        return ResponseEntity.ok(bookingService.checkInBooking(request.getBookingCode()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookingsDTO());
    }

    @GetMapping("/code/{bookingCode}")
    public ResponseEntity<BookingResponseDTO> getBookingByCode(@PathVariable String bookingCode) {
        return ResponseEntity.ok(bookingService.getBookingDTOByCode(bookingCode));
    }
}
