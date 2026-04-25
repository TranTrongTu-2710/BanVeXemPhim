package com.example.demo.services;

import com.example.demo.model.*;
import com.example.demo.repository.BookingSeatRepository;
import com.example.demo.repository.SeatPriceRepository;
import com.example.demo.repository.SeatRepository;
import com.example.demo.repository.ShowtimeRepository;
import com.example.demo.response.ErrorResponse;
import com.example.demo.response.SeatStatusResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatSelectionService {

    private final StringRedisTemplate redisTemplate;
    private final ShowtimeRepository showtimeRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatPriceRepository seatPriceRepository;
    private final SeatRepository seatRepository; // Inject SeatRepository
    private static final Logger logger = LoggerFactory.getLogger(SeatSelectionService.class);


    private static final String SEAT_LOCK_KEY_PREFIX = "seat_locks:";
    private static final String USER_SELECTION_COUNT_KEY_PREFIX = "user_selections:";
    private static final int MAX_SEATS_PER_USER = 10;
    private static final long LOCK_TIMEOUT_MINUTES = 10;

    public List<SeatStatusResponse> getSeatStatusesForShowtime(Integer showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Showtime not found with id: " + showtimeId));
        Screen screen = showtime.getScreen();
        List<Seat> allSeatsInScreen = screen.getSeats();

        Set<Integer> bookedSeatIds = bookingSeatRepository.findBookedSeatIdsByShowtime(showtimeId);

        String seatLockKey = SEAT_LOCK_KEY_PREFIX + showtimeId;
        Map<Object, Object> lockedSeatsMap = redisTemplate.opsForHash().entries(seatLockKey);

        List<SeatPrice> prices = seatPriceRepository.findByShowtime(showtime);

        return allSeatsInScreen.stream()
                .filter(Seat::getIsActive)
                .map(seat -> {
                    SeatStatusResponse response = new SeatStatusResponse();
                    response.setId(seat.getId());
                    response.setRowName(seat.getRowName());
                    response.setSeatNumber(seat.getSeatNumber());
                    response.setSeatType(seat.getSeatType());

                    BigDecimal price = prices.stream()
                            .filter(p -> p.getSeatType() == seat.getSeatType())
                            .map(SeatPrice::getPrice)
                            .findFirst()
                            .orElse(BigDecimal.valueOf(45000.0));
                    response.setPrice(price.doubleValue());

                    if (bookedSeatIds.contains(seat.getId())) {
                        response.setStatus("BOOKED");
                    } else if (lockedSeatsMap.containsKey(String.valueOf(seat.getId()))) {
                        response.setStatus("LOCKED");
                        String userIdStr = (String) lockedSeatsMap.get(String.valueOf(seat.getId()));
                        if (userIdStr != null) {
                            try {
                                response.setUserId(Integer.parseInt(userIdStr));
                            } catch (NumberFormatException e) {
                                logger.warn("Could not parse userId from Redis for showtime {} and seat {}. Value was: '{}'. This might be due to old data format.", showtimeId, seat.getId(), userIdStr);
                            }
                        }
                    } else {
                        response.setStatus("AVAILABLE");
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }

    public BigDecimal calculateTotalPrice(Integer showtimeId, List<Integer> seatIds) {
        List<SeatPrice> prices = seatPriceRepository.findByShowtimeId(showtimeId);
        List<Seat> selectedSeats = seatRepository.findAllById(seatIds);

        return selectedSeats.stream()
                .map(seat -> getSeatPrice(prices, seat.getSeatType()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getSeatPrice(Showtime showtime, Seat.SeatType seatType) {
        return getSeatPrice(showtime.getSeatPrices(), seatType);
    }

    private BigDecimal getSeatPrice(List<SeatPrice> prices, Seat.SeatType seatType) {
        return prices.stream()
                .filter(p -> p.getSeatType() == seatType)
                .map(SeatPrice::getPrice)
                .findFirst()
                .orElse(BigDecimal.valueOf(45000.0)); // Giá mặc định
    }

    public Object selectSeat(Integer showtimeId, Integer seatId, Integer userId) {
        String userIdStr = String.valueOf(userId);
        String seatIdStr = String.valueOf(seatId);
        String seatLockKey = SEAT_LOCK_KEY_PREFIX + showtimeId;
        String userSelectionCountKey = USER_SELECTION_COUNT_KEY_PREFIX + showtimeId;

        String currentCountStr = (String) redisTemplate.opsForHash().get(userSelectionCountKey, userIdStr);
        int currentCount = currentCountStr == null ? 0 : Integer.parseInt(currentCountStr);

        if (currentCount >= MAX_SEATS_PER_USER) {
            return new ErrorResponse("Bạn chỉ được chọn tối đa " + MAX_SEATS_PER_USER + " ghế.");
        }

        Boolean isLocked = redisTemplate.opsForHash().putIfAbsent(seatLockKey, seatIdStr, userIdStr);

        if (!isLocked) {
            String lockingUserId = (String) redisTemplate.opsForHash().get(seatLockKey, seatIdStr);
            if (userIdStr.equals(lockingUserId)) {
                String individualSeatKey = seatLockKey + ":" + seatIdStr;
                redisTemplate.expire(individualSeatKey, LOCK_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                return Map.of("status", "success", "message", "Gia hạn khóa ghế thành công.", "seatId", seatId, "userId", userId);
            }
            return new ErrorResponse("Ghế này đang được người khác chọn.");
        }

        String individualSeatKey = seatLockKey + ":" + seatIdStr;
        redisTemplate.opsForValue().set(individualSeatKey, userIdStr, LOCK_TIMEOUT_MINUTES, TimeUnit.MINUTES);

        redisTemplate.opsForHash().put(userSelectionCountKey, userIdStr, String.valueOf(currentCount + 1));

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Chọn ghế thành công!");
        response.put("seatId", seatId);
        response.put("userId", userId);
        return response;
    }

    public Object deselectSeat(Integer showtimeId, Integer seatId, Integer userId) {
        String userIdStr = String.valueOf(userId);
        String seatIdStr = String.valueOf(seatId);
        String seatLockKey = SEAT_LOCK_KEY_PREFIX + showtimeId;
        String userSelectionCountKey = USER_SELECTION_COUNT_KEY_PREFIX + showtimeId;
        String individualSeatKey = seatLockKey + ":" + seatIdStr;

        String lockingUserId = (String) redisTemplate.opsForHash().get(seatLockKey, seatIdStr);

        if (!userIdStr.equals(lockingUserId)) {
            return new ErrorResponse("Bạn không thể bỏ chọn ghế của người khác.");
        }

        redisTemplate.opsForHash().delete(seatLockKey, seatIdStr);
        redisTemplate.delete(individualSeatKey);

        long newCount = redisTemplate.opsForHash().increment(userSelectionCountKey, userIdStr, -1);

        if (newCount <= 0) {
            redisTemplate.opsForHash().delete(userSelectionCountKey, userIdStr);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "deselected");
        response.put("seatId", seatId);
        response.put("userId", userId);
        return response;
    }
    
    public void releaseExpiredSeat(Integer showtimeId, int seatId) {
        String seatIdStr = String.valueOf(seatId);
        String seatLockKey = SEAT_LOCK_KEY_PREFIX + showtimeId;
        String userSelectionCountKey = USER_SELECTION_COUNT_KEY_PREFIX + showtimeId;

        String userIdStr = (String) redisTemplate.opsForHash().get(seatLockKey, seatIdStr);

        if (userIdStr != null) {
            redisTemplate.opsForHash().delete(seatLockKey, seatIdStr);

            long newCount = redisTemplate.opsForHash().increment(userSelectionCountKey, userIdStr, -1);
            if (newCount <= 0) {
                redisTemplate.opsForHash().delete(userSelectionCountKey, userIdStr);
            }
            System.out.println("Successfully released seat " + seatId + " for user " + userIdStr);
        }
    }
}
