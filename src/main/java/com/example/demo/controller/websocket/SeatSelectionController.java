package com.example.demo.controller.websocket;

import com.example.demo.request.SeatSelectionRequest;
import com.example.demo.services.SeatSelectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class SeatSelectionController {

    private final SimpMessagingTemplate messagingTemplate;
    private final SeatSelectionService seatSelectionService;

    @MessageMapping("/showtimes/{showtimeId}/select-seat")
    public void selectSeat(@DestinationVariable Integer showtimeId,
                           @Payload SeatSelectionRequest request) {
        
        // Lấy userId từ payload do client gửi lên
        Integer userId = request.getUserId();
        Integer seatId = request.getSeatId();

        if (userId == null || seatId == null) {
            // Xử lý trường hợp dữ liệu không hợp lệ
            return;
        }

        Object result = seatSelectionService.selectSeat(showtimeId, seatId, userId);
        messagingTemplate.convertAndSend("/topic/showtimes/" + showtimeId + "/seats", result);
    }

    @MessageMapping("/showtimes/{showtimeId}/deselect-seat")
    public void deselectSeat(@DestinationVariable Integer showtimeId,
                             @Payload SeatSelectionRequest request) {
        
        // Lấy userId từ payload do client gửi lên
        Integer userId = request.getUserId();
        Integer seatId = request.getSeatId();

        if (userId == null || seatId == null) {
            // Xử lý trường hợp dữ liệu không hợp lệ
            return;
        }

        Object result = seatSelectionService.deselectSeat(showtimeId, seatId, userId);
        messagingTemplate.convertAndSend("/topic/showtimes/" + showtimeId + "/seats", result);
    }
}
