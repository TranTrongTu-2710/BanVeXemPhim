package com.example.demo.services.serviceIplm;

import com.example.demo.email.EmailService;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.request.FoodItemRequest;
import com.example.demo.request.booking.CreateBookingForCustomerRequest;
import com.example.demo.request.booking.CreateBookingRequest;
import com.example.demo.response.BookingResponseDTO;
import com.example.demo.response.PendingBookingDTO;
import com.example.demo.services.BookingService;
import com.example.demo.services.NotificationService;
import com.example.demo.services.SeatSelectionService;
import com.example.demo.services.SecurityService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final FoodItemRepository foodItemRepository;
    private final UserRepository userRepository;
    private final PromotionRepository promotionRepository;
    private final UserPromotionRepository userPromotionRepository;
    private final SecurityService securityService;
    private final SeatSelectionService seatSelectionService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Value("${server.public-url}")
    private String serverPublicUrl;

    @Override
    public PendingBookingDTO calculateBookingDetails(PendingBookingDTO pendingBooking) {
        User user = userRepository.findById(pendingBooking.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Showtime showtime = showtimeRepository.findById(pendingBooking.getShowtimeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Showtime not found"));
        List<Seat> seats = seatRepository.findAllById(pendingBooking.getSeatIds());

        if (seats.size() != pendingBooking.getSeatIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more seats not found.");
        }

        BigDecimal totalSeatAmount = calculateTotalAmount(showtime, seats);
        BigDecimal totalFoodAmount = calculateFoodAmount(pendingBooking.getFoodItems());
        BigDecimal totalAmount = totalSeatAmount.add(totalFoodAmount);

        // Xử lý khuyến mãi
        Promotion appliedPromotion = null;
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (pendingBooking.getPromotionCode() != null && !pendingBooking.getPromotionCode().trim().isEmpty()) {
            try {
                appliedPromotion = findAndValidatePromotion(
                        pendingBooking.getPromotionCode().trim(),
                        user,
                        totalAmount
                );
                discountAmount = calculateDiscount(totalAmount, appliedPromotion);
                log.info("Applied promotion {} with discount amount: {}",
                        appliedPromotion.getCode(), discountAmount);
            } catch (ResponseStatusException e) {
                log.warn("Promotion validation failed: {}", e.getReason());
                // Re-throw để client biết lỗi cụ thể
                throw e;
            }
        }

        // Tính toán giảm giá từ điểm tích lũy
        BigDecimal pointsDiscount = BigDecimal.ZERO;
        if (pendingBooking.getPointsUsed() != null && pendingBooking.getPointsUsed() > 0) {
            if (user.getPoints() < pendingBooking.getPointsUsed()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Not enough points. Available: " + user.getPoints() + ", Required: " + pendingBooking.getPointsUsed());
            }
            // 1 điểm = 100 VND
            pointsDiscount = new BigDecimal(pendingBooking.getPointsUsed()).multiply(new BigDecimal("100"));
        }

        BigDecimal totalDiscountAmount = discountAmount.add(pointsDiscount);
        BigDecimal finalAmount = totalAmount.subtract(totalDiscountAmount);

        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        pendingBooking.setTotalAmount(totalAmount);
        pendingBooking.setDiscountAmount(totalDiscountAmount);
        pendingBooking.setFinalAmount(finalAmount);

        return pendingBooking;
    }

    @Override
    @Transactional
    public Booking createBookingFromPending(PendingBookingDTO pendingBooking) {
        User user = userRepository.findById(pendingBooking.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Showtime showtime = showtimeRepository.findById(pendingBooking.getShowtimeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Showtime not found"));
        List<Seat> seats = seatRepository.findAllById(pendingBooking.getSeatIds());

        if (seats.size() != pendingBooking.getSeatIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more seats not found.");
        }

        // Validate promotion again before creating booking
        Promotion appliedPromotion = null;
        if (pendingBooking.getPromotionCode() != null && !pendingBooking.getPromotionCode().trim().isEmpty()) {
            appliedPromotion = findAndValidatePromotion(
                    pendingBooking.getPromotionCode().trim(),
                    user,
                    pendingBooking.getTotalAmount()
            );
        }

        // Trừ điểm của user nếu có sử dụng
        if (pendingBooking.getPointsUsed() != null && pendingBooking.getPointsUsed() > 0) {
            deductPointsFromUser(user, pendingBooking.getPointsUsed());
        }

        Booking booking = Booking.builder()
                .user(user)
                .showtime(showtime)
                .bookingCode(generateBookingCode())
                .totalAmount(pendingBooking.getTotalAmount())
                .discountAmount(pendingBooking.getDiscountAmount())
                .finalAmount(pendingBooking.getFinalAmount())
                .paymentMethod(Booking.PaymentMethod.vnpay)
                .paymentStatus(Booking.PaymentStatus.paid)
                .bookingStatus(Booking.BookingStatus.confirmed)
                .bookingDate(LocalDateTime.now())
                .paymentDate(LocalDateTime.now())
                .notes(pendingBooking.getNotes())
                .pointsUsed(pendingBooking.getPointsUsed() != null ? pendingBooking.getPointsUsed() : 0)
                .build();

        String qrCodeUrl = serverPublicUrl + "/bookings/qr/" + booking.getBookingCode();
        booking.setQrCode(qrCodeUrl);

        List<BookingSeat> bookingSeats = seats.stream()
                .map(seat -> BookingSeat.builder()
                        .booking(booking)
                        .seat(seat)
                        .price(seatSelectionService.getSeatPrice(showtime, seat.getSeatType()))
                        .build())
                .collect(Collectors.toList());
        booking.setBookingSeats(bookingSeats);

        if (pendingBooking.getFoodItems() != null && !pendingBooking.getFoodItems().isEmpty()) {
            List<BookingFood> bookingFoods = pendingBooking.getFoodItems().stream()
                    .map(itemRequest -> {
                        FoodItem foodItem = foodItemRepository.findById(itemRequest.getFoodItemId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food item not found."));
                        return BookingFood.builder()
                                .booking(booking)
                                .foodItem(foodItem)
                                .quantity(itemRequest.getQuantity())
                                .price(foodItem.getPrice())
                                .build();
                    })
                    .collect(Collectors.toList());
            booking.setBookingFoods(bookingFoods);
        }

        // Update available seats
        showtime.setAvailableSeats(showtime.getAvailableSeats() - seats.size());
        showtimeRepository.save(showtime);

        // Calculate and set points earned
        Integer pointsEarned = calculatePointsEarned(booking.getFinalAmount());
        booking.setPointsEarned(pointsEarned);

        // Save booking first
        Booking savedBooking = bookingRepository.save(booking);

        // Log promotion usage after booking is saved
        if (appliedPromotion != null) {
            logPromotionUsage(user, appliedPromotion, savedBooking);
        }

        // Add earned points to user
        addPointsToUser(user, pointsEarned);

        // Send notifications - these should not fail the booking transaction
        try {
            emailService.sendBookingConfirmationEmail(savedBooking);
        } catch (Exception e) {
            log.error("Failed to send email for booking {}: {}", savedBooking.getBookingCode(), e.getMessage());
        }

        try {
            createBookingNotification(savedBooking);
        } catch (Exception e) {
            log.error("Failed to create notification for booking {}: {}", savedBooking.getBookingCode(), e.getMessage());
        }

        log.info("Booking created successfully. Code: {}, Total: {}, Discount: {}, Final: {}",
                savedBooking.getBookingCode(),
                savedBooking.getTotalAmount(),
                savedBooking.getDiscountAmount(),
                savedBooking.getFinalAmount());

        return savedBooking;
    }

    @Override
    @Transactional
    public BookingResponseDTO createBooking(CreateBookingRequest request) {
        User currentUser = securityService.getCurrentUser();
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Showtime not found."));

        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());

        if (seats.size() != request.getSeatIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more seats not found.");
        }

        // 1. Tính Tổng Tiền Gốc (Total Amount)
        BigDecimal totalSeatAmount = calculateTotalAmount(showtime, seats);
        BigDecimal totalFoodAmount = calculateFoodAmountFromOldRequest(request.getFoodItems());
        BigDecimal totalAmount = totalSeatAmount.add(totalFoodAmount);

        log.debug("Booking calculation - Total Seat: {}, Total Food: {}, Grand Total: {}",
                totalSeatAmount, totalFoodAmount, totalAmount);

        // 2. Xử lý Khuyến Mãi (Promotion) - FIX: Không bắt exception nữa, để nó throw ra ngoài
        Promotion appliedPromotion = null;
        BigDecimal promoDiscount = BigDecimal.ZERO;

        if (request.getPromotionCode() != null && !request.getPromotionCode().trim().isEmpty()) {
            appliedPromotion = findAndValidatePromotion(
                    request.getPromotionCode().trim(),
                    currentUser,
                    totalAmount
            );
            promoDiscount = calculateDiscount(totalAmount, appliedPromotion);
            log.info("Applied promotion code: {}, Discount: {}",
                    appliedPromotion.getCode(), promoDiscount);
        }

        // 3. Xử lý Điểm Tích Lũy (Points)
        BigDecimal pointsDiscount = BigDecimal.ZERO;
        Integer pointsUsed = request.getPointsUsed();

        if (pointsUsed != null && pointsUsed > 0) {
            if (currentUser.getPoints() < pointsUsed) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Not enough points. Available: " + currentUser.getPoints() + ", Required: " + pointsUsed);
            }
            // Quy đổi: 1 điểm = 100 VND
            pointsDiscount = new BigDecimal(pointsUsed).multiply(new BigDecimal("100"));
            log.debug("Points used: {}, Points discount: {}", pointsUsed, pointsDiscount);
        }

        // 4. Tính Tổng Giảm Giá và Giá Cuối Cùng
        BigDecimal totalDiscount = promoDiscount.add(pointsDiscount);
        BigDecimal finalAmount = totalAmount.subtract(totalDiscount);

        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Final amount is negative, setting to zero. Original: {}", finalAmount);
            finalAmount = BigDecimal.ZERO;
        }

        log.info("Booking summary - Total: {}, Promo Discount: {}, Points Discount: {}, Final: {}",
                totalAmount, promoDiscount, pointsDiscount, finalAmount);

        // 5. Trừ điểm của user trước khi tạo booking
        if (pointsUsed != null && pointsUsed > 0) {
            deductPointsFromUser(currentUser, pointsUsed);
        }

        // 6. Tạo Booking với đầy đủ thông tin
        Booking booking = buildBookingWithFullInfo(
                request,
                currentUser,
                showtime,
                seats,
                totalAmount,
                totalDiscount,
                finalAmount
        );

        // Lưu thông tin điểm đã dùng
        booking.setPointsUsed(pointsUsed != null ? pointsUsed : 0);

        // Generate QR code
        String qrCodeUrl = serverPublicUrl + "/bookings/qr/" + booking.getBookingCode();
        booking.setQrCode(qrCodeUrl);

        // Tính điểm thưởng (dựa trên số tiền thực trả)
        Integer pointsEarned = calculatePointsEarned(finalAmount);
        booking.setPointsEarned(pointsEarned);

        // 7. Lưu booking trước
        Booking savedBooking = bookingRepository.save(booking);

        // 8. Ghi nhận lịch sử dùng khuyến mãi sau khi booking đã được lưu
        if (appliedPromotion != null) {
            logPromotionUsage(currentUser, appliedPromotion, savedBooking);
        }

        // 9. Cộng điểm thưởng
        addPointsToUser(currentUser, pointsEarned);

        // 10. Send notifications - these should not fail the booking transaction
        try {
            emailService.sendBookingConfirmationEmail(savedBooking);
        } catch (Exception e) {
            log.error("Failed to send email for booking {}: {}", savedBooking.getBookingCode(), e.getMessage());
        }

        try {
            createBookingNotification(savedBooking);
        } catch (Exception e) {
            log.error("Failed to create notification for booking {}: {}", savedBooking.getBookingCode(), e.getMessage());
        }

        log.info("Booking created successfully. Code: {}, User: {}, Final Amount: {}",
                savedBooking.getBookingCode(), currentUser.getId(), savedBooking.getFinalAmount());

        return new BookingResponseDTO(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDTO createBookingForCustomer(CreateBookingForCustomerRequest request) {
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Showtime not found."));

        securityService.checkStaffCinemaAccess(showtime.getScreen().getCinema().getId());

        User customer = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer user not found."));

        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());

        if (seats.size() != request.getSeatIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more seats not found.");
        }

        BigDecimal totalSeatAmount = calculateTotalAmount(showtime, seats);
        BigDecimal totalFoodAmount = calculateFoodAmountFromOldRequest(request.getFoodItems());
        BigDecimal totalAmount = totalSeatAmount.add(totalFoodAmount);

        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal finalAmount = totalAmount;

        Booking booking = buildBookingWithFullInfo(
                request,
                customer,
                showtime,
                seats,
                totalAmount,
                discountAmount,
                finalAmount
        );

        booking.setBookingStatus(Booking.BookingStatus.completed);
        booking.setPaymentStatus(Booking.PaymentStatus.paid);
        booking.setPaymentDate(LocalDateTime.now());

        String qrCodeUrl = serverPublicUrl + "/bookings/qr/" + booking.getBookingCode();
        booking.setQrCode(qrCodeUrl);

        Integer pointsEarned = calculatePointsEarned(finalAmount);
        booking.setPointsEarned(pointsEarned);

        Booking savedBooking = bookingRepository.save(booking);

        addPointsToUser(customer, pointsEarned);

        try {
            emailService.sendBookingConfirmationEmail(savedBooking);
        } catch (Exception e) {
            log.error("Failed to send email for booking {}: {}", savedBooking.getBookingCode(), e.getMessage());
        }

        try {
            createBookingNotification(savedBooking);
        } catch (Exception e) {
            log.error("Failed to create notification for booking {}: {}", savedBooking.getBookingCode(), e.getMessage());
        }

        log.info("Staff booking created for customer {}. Code: {}", customer.getId(), savedBooking.getBookingCode());

        return new BookingResponseDTO(savedBooking);
    }

    // --- Helper Methods ---

    /**
     * Tính điểm thưởng dựa trên số tiền thanh toán
     * 1000 VND = 1 điểm
     */
    private Integer calculatePointsEarned(BigDecimal finalAmount) {
        if (finalAmount == null || finalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return finalAmount.divide(new BigDecimal("1000"), 0, RoundingMode.DOWN).intValue();
    }

    /**
     * Cộng điểm vào tài khoản user
     */
    private void addPointsToUser(User user, Integer pointsToAdd) {
        if (pointsToAdd != null && pointsToAdd > 0) {
            Integer currentPoints = user.getPoints() == null ? 0 : user.getPoints();
            user.setPoints(currentPoints + pointsToAdd);
            userRepository.save(user);
            log.debug("Added {} points to user {}. New balance: {}",
                    pointsToAdd, user.getId(), user.getPoints());
        }
    }

    /**
     * Trừ điểm từ tài khoản user
     */
    private void deductPointsFromUser(User user, Integer pointsToDeduct) {
        if (pointsToDeduct == null || pointsToDeduct <= 0) {
            return;
        }

        Integer currentPoints = user.getPoints() == null ? 0 : user.getPoints();
        if (currentPoints < pointsToDeduct) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "User does not have enough points. Available: " + currentPoints + ", Required: " + pointsToDeduct);
        }
        user.setPoints(currentPoints - pointsToDeduct);
        userRepository.save(user);
        log.debug("Deducted {} points from user {}. New balance: {}",
                pointsToDeduct, user.getId(), user.getPoints());
    }

    /**
     * Tạo thông báo cho booking
     */
    private void createBookingNotification(Booking booking) {
        try {
            String title = "Đặt vé thành công: " + booking.getShowtime().getMovie().getTitle() +
                    " - " + booking.getShowtime().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));

            StringBuilder message = new StringBuilder();
            message.append("<b>Mã vé:</b> ").append(booking.getBookingCode()).append("<br>");
            message.append("<b>Rạp:</b> ").append(booking.getShowtime().getScreen().getCinema().getName()).append("<br>");
            message.append("<b>Phòng chiếu:</b> ").append(booking.getShowtime().getScreen().getName()).append("<br>");
            message.append("<b>Suất chiếu:</b> ").append(booking.getShowtime().getShowDate())
                    .append(" ").append(booking.getShowtime().getStartTime()).append("<br>");

            String seats = booking.getBookingSeats().stream()
                    .map(bs -> bs.getSeat().getRowName() + bs.getSeat().getSeatNumber())
                    .collect(Collectors.joining(", "));
            message.append("<b>Ghế:</b> ").append(seats).append("<br>");

            if (booking.getBookingFoods() != null && !booking.getBookingFoods().isEmpty()) {
                String foods = booking.getBookingFoods().stream()
                        .map(bf -> bf.getFoodItem().getName() + " x" + bf.getQuantity())
                        .collect(Collectors.joining(", "));
                message.append("<b>Đồ ăn:</b> ").append(foods).append("<br>");
            }

            message.append("<b>Tổng tiền:</b> ").append(String.format("%,.0f", booking.getTotalAmount())).append(" đ<br>");

            if (booking.getDiscountAmount() != null && booking.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                message.append("<b>Giảm giá:</b> -").append(String.format("%,.0f", booking.getDiscountAmount())).append(" đ<br>");
            }

            message.append("<b>Thanh toán:</b> ").append(String.format("%,.0f", booking.getFinalAmount())).append(" đ<br>");

            if (booking.getPointsUsed() != null && booking.getPointsUsed() > 0) {
                message.append("<b>Điểm đã dùng:</b> -").append(booking.getPointsUsed()).append("<br>");
            }
            if (booking.getPointsEarned() != null && booking.getPointsEarned() > 0) {
                message.append("<b>Điểm tích lũy:</b> +").append(booking.getPointsEarned());
            }

            notificationService.createNotification(
                    booking.getUser(),
                    Notification.NotificationType.booking,
                    title,
                    message.toString(),
                    booking.getId()
            );
        } catch (Exception e) {
            log.error("Error creating notification for booking {}: {}", booking.getId(), e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public BookingResponseDTO checkInBooking(String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking with this code not found."));

        securityService.checkStaffCinemaAccess(booking.getShowtime().getScreen().getCinema().getId());

        if (booking.getBookingStatus() == Booking.BookingStatus.completed) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This ticket has already been checked in.");
        }
        if (booking.getBookingStatus() == Booking.BookingStatus.cancelled) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This ticket has been cancelled.");
        }
        if (booking.getBookingStatus() != Booking.BookingStatus.confirmed) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This ticket is not confirmed yet. Payment may be pending.");
        }

        booking.setBookingStatus(Booking.BookingStatus.completed);
        Booking updatedBooking = bookingRepository.save(booking);

        log.info("Booking checked in successfully. Code: {}", bookingCode);

        return new BookingResponseDTO(updatedBooking);
    }

    /**
     * Build booking object với đầy đủ thông tin bao gồm payment status và booking date
     */
    private Booking buildBookingWithFullInfo(CreateBookingRequest request, User customer, Showtime showtime,
                                             List<Seat> seats, BigDecimal totalAmount,
                                             BigDecimal discountAmount, BigDecimal finalAmount) {
        Booking booking = Booking.builder()
                .user(customer)
                .showtime(showtime)
                .bookingCode(generateBookingCode())
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(Booking.PaymentStatus.pending)  // Set initial status
                .bookingStatus(Booking.BookingStatus.pending)  // Set initial status
                .bookingDate(LocalDateTime.now())              // Set booking date
                .notes(request.getNotes())
                .build();

        List<BookingSeat> bookingSeats = seats.stream()
                .map(seat -> BookingSeat.builder()
                        .booking(booking)
                        .seat(seat)
                        .price(getSeatPrice(showtime, seat.getSeatType()))
                        .build())
                .collect(Collectors.toList());
        booking.setBookingSeats(bookingSeats);

        if (request.getFoodItems() != null && !request.getFoodItems().isEmpty()) {
            List<BookingFood> bookingFoods = request.getFoodItems().stream()
                    .map(itemRequest -> {
                        FoodItem foodItem = foodItemRepository.findById(itemRequest.getFoodItemId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food item not found."));
                        if (!foodItem.getIsAvailable()) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "Food item " + foodItem.getName() + " is not available.");
                        }
                        return BookingFood.builder()
                                .booking(booking)
                                .foodItem(foodItem)
                                .quantity(itemRequest.getQuantity())
                                .price(foodItem.getPrice())
                                .build();
                    })
                    .collect(Collectors.toList());
            booking.setBookingFoods(bookingFoods);
        }

        // Update available seats
        showtime.setAvailableSeats(showtime.getAvailableSeats() - seats.size());
        showtimeRepository.save(showtime);

        return booking;
    }

    /**
     * Legacy method - kept for backward compatibility
     * @deprecated Use buildBookingWithFullInfo instead
     */
    @Deprecated
    private Booking buildBooking(CreateBookingRequest request, User customer, Showtime showtime,
                                 List<Seat> seats, BigDecimal totalAmount,
                                 BigDecimal discountAmount, BigDecimal finalAmount) {
        return buildBookingWithFullInfo(request, customer, showtime, seats, totalAmount, discountAmount, finalAmount);
    }

    /**
     * Tính tổng tiền đồ ăn từ PendingBookingDTO
     */
    private BigDecimal calculateFoodAmount(List<FoodItemRequest> foodItems) {
        if (foodItems == null || foodItems.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return foodItems.stream()
                .map(item -> {
                    FoodItem foodItem = foodItemRepository.findById(item.getFoodItemId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                    "Food item with ID " + item.getFoodItemId() + " not found."));
                    if (!foodItem.getIsAvailable()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Food item " + foodItem.getName() + " is not available.");
                    }
                    return foodItem.getPrice().multiply(new BigDecimal(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Tính tổng tiền đồ ăn từ CreateBookingRequest
     */
    private BigDecimal calculateFoodAmountFromOldRequest(List<CreateBookingRequest.FoodOrderItem> foodItems) {
        if (foodItems == null || foodItems.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalFoodAmount = BigDecimal.ZERO;
        for (CreateBookingRequest.FoodOrderItem itemRequest : foodItems) {
            FoodItem foodItem = foodItemRepository.findById(itemRequest.getFoodItemId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Food item with ID " + itemRequest.getFoodItemId() + " not found."));
            if (!foodItem.getIsAvailable()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Food item " + foodItem.getName() + " is not available.");
            }
            totalFoodAmount = totalFoodAmount.add(
                    foodItem.getPrice().multiply(new BigDecimal(itemRequest.getQuantity()))
            );
        }
        return totalFoodAmount;
    }

    /**
     * Tìm và validate promotion
     * Throws ResponseStatusException nếu promotion không hợp lệ
     */
    private Promotion findAndValidatePromotion(String promotionCode, User user, BigDecimal totalAmount) {
        if (promotionCode == null || promotionCode.trim().isBlank()) {
            return null;
        }

        Promotion promotion = promotionRepository.findByCode(promotionCode.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Promotion code '" + promotionCode + "' is invalid or does not exist."));

        validatePromotion(promotion, user, totalAmount);

        return promotion;
    }

    /**
     * Ghi nhận việc sử dụng promotion
     */
    private void logPromotionUsage(User customer, Promotion promotion, Booking booking) {
        if (promotion == null) {
            return;
        }

        try {
            UserPromotion userPromotion = UserPromotion.builder()
                    .user(customer)
                    .promotion(promotion)
                    .booking(booking)
                    .usedAt(LocalDateTime.now())
                    .build();
            userPromotionRepository.save(userPromotion);

            // Increment usage count
            promotion.setUsedCount(promotion.getUsedCount() + 1);
            promotionRepository.save(promotion);

            log.info("Logged promotion usage. Code: {}, User: {}, Booking: {}",
                    promotion.getCode(), customer.getId(), booking.getBookingCode());
        } catch (Exception e) {
            log.error("Error logging promotion usage: {}", e.getMessage(), e);
            // Don't throw exception here to avoid breaking the booking flow
        }
    }

    /**
     * Tính tổng tiền ghế
     */
    private BigDecimal calculateTotalAmount(Showtime showtime, List<Seat> seats) {
        return seats.stream()
                .map(seat -> getSeatPrice(showtime, seat.getSeatType()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Lấy giá ghế theo loại
     */
    private BigDecimal getSeatPrice(Showtime showtime, Seat.SeatType seatType) {
        return showtime.getSeatPrices().stream()
                .filter(p -> p.getSeatType() == seatType)
                .findFirst()
                .map(SeatPrice::getPrice)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Price for seat type " + seatType + " not defined for this showtime."));
    }

    /**
     * Validate promotion với các điều kiện:
     * - Active và chưa hết hạn
     * - Chưa hết lượt sử dụng
     * - Đủ điều kiện giá trị đơn hàng tối thiểu
     * - Đủ rank membership
     */
    private void validatePromotion(Promotion promotion, User user, BigDecimal totalAmount) {
        // Check active status and expiry
        if (!promotion.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Promotion code '" + promotion.getCode() + "' is not active.");
        }

        if (promotion.getValidTo().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Promotion code '" + promotion.getCode() + "' has expired.");
        }

        // Check usage limit
        if (promotion.getUsageLimit() != null && promotion.getUsedCount() >= promotion.getUsageLimit()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Promotion code '" + promotion.getCode() + "' has reached its usage limit.");
        }

        // Check minimum purchase amount
        if (promotion.getMinPurchaseAmount() != null &&
                totalAmount.compareTo(promotion.getMinPurchaseAmount()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Minimum purchase amount of " + promotion.getMinPurchaseAmount() +
                            " VND is required for this promotion. Current total: " + totalAmount + " VND");
        }

        // Check user rank eligibility
        Promotion.RankUserApply requiredRank = promotion.getRankUserApply();
        if (requiredRank != null && requiredRank != Promotion.RankUserApply.ALL) {
            User.MembershipTier userTier = user.getMembershipTier();
            boolean isEligible = false;

            if (requiredRank == Promotion.RankUserApply.GOLD &&
                    (userTier == User.MembershipTier.gold || userTier == User.MembershipTier.platinum)) {
                isEligible = true;
            } else if (requiredRank == Promotion.RankUserApply.PLATINUM &&
                    userTier == User.MembershipTier.platinum) {
                isEligible = true;
            }

            if (!isEligible) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Your membership tier (" + userTier + ") is not eligible for this promotion. " +
                                "Required tier: " + requiredRank);
            }
        }
    }

    /**
     * Tính toán số tiền giảm giá từ promotion
     * Hỗ trợ 2 loại: percentage và fixed_amount
     */
    private BigDecimal calculateDiscount(BigDecimal totalAmount, Promotion promotion) {
        if (promotion == null) {
            return BigDecimal.ZERO;
        }

        if (promotion.getDiscountType() == Promotion.DiscountType.percentage) {
            // Tính % giảm giá
            BigDecimal discount = totalAmount.multiply(
                    promotion.getDiscountValue().divide(new BigDecimal(100), 2, RoundingMode.HALF_UP)
            );

            // Kiểm tra max discount amount nếu có
            if (promotion.getMaxDiscountAmount() != null &&
                    discount.compareTo(promotion.getMaxDiscountAmount()) > 0) {
                return promotion.getMaxDiscountAmount();
            }

            return discount.setScale(0, RoundingMode.HALF_UP);

        } else if (promotion.getDiscountType() == Promotion.DiscountType.fixed_amount) {
            // Giảm giá cố định
            BigDecimal discount = promotion.getDiscountValue();

            // Đảm bảo không giảm quá tổng tiền
            if (discount.compareTo(totalAmount) > 0) {
                return totalAmount;
            }

            return discount;
        }

        log.warn("Unknown discount type: {}", promotion.getDiscountType());
        return BigDecimal.ZERO;
    }

    /**
     * Generate unique booking code
     */
    private String generateBookingCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    public BookingResponseDTO getBookingDTOById(Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
        return new BookingResponseDTO(booking);
    }

    @Override
    public BookingResponseDTO getBookingDTOByCode(String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Booking with code '" + bookingCode + "' not found."));
        return new BookingResponseDTO(booking);
    }

    @Override
    public List<BookingResponseDTO> getMyBookingsDTO() {
        User currentUser = securityService.getCurrentUser();
        return bookingRepository.findByUserId(currentUser.getId()).stream()
                .map(BookingResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDTO> getAllBookingsDTO() {
        return bookingRepository.findAll().stream()
                .map(BookingResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Booking cancelBooking(Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));

        User currentUser = securityService.getCurrentUser();

        // Check authorization
        if (!booking.getUser().getId().equals(currentUser.getId()) &&
                !(currentUser.getRole() == User.Role.admin || currentUser.getRole() == User.Role.staff)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not authorized to cancel this booking.");
        }

        // Staff must have access to the cinema
        if(currentUser.getRole() == User.Role.staff) {
            securityService.checkStaffCinemaAccess(booking.getShowtime().getScreen().getCinema().getId());
        }

        // Check if booking can be cancelled
        if (booking.getBookingStatus() == Booking.BookingStatus.cancelled) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Booking has already been cancelled.");
        }

        if (booking.getBookingStatus() == Booking.BookingStatus.completed) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot cancel a completed booking.");
        }

        // Update booking status
        booking.setBookingStatus(Booking.BookingStatus.cancelled);
        booking.setPaymentStatus(Booking.PaymentStatus.refunded);

        // Restore seats availability
        Showtime showtime = booking.getShowtime();
        showtime.setAvailableSeats(showtime.getAvailableSeats() + booking.getBookingSeats().size());
        showtimeRepository.save(showtime);

        // Refund points if used
        if (booking.getPointsUsed() != null && booking.getPointsUsed() > 0) {
            addPointsToUser(booking.getUser(), booking.getPointsUsed());
        }

        // Deduct earned points
        if (booking.getPointsEarned() != null && booking.getPointsEarned() > 0) {
            deductPointsFromUser(booking.getUser(), booking.getPointsEarned());
        }

        Booking cancelledBooking = bookingRepository.save(booking);

        log.info("Booking cancelled. Code: {}, User: {}", booking.getBookingCode(), currentUser.getId());

        return cancelledBooking;
    }
}