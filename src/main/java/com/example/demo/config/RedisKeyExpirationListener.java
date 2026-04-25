package com.example.demo.config;

import com.example.demo.services.SeatSelectionService;
import lombok.NonNull;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final SeatSelectionService seatSelectionService;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer,
                                      SimpMessagingTemplate messagingTemplate,
                                      SeatSelectionService seatSelectionService) {
        super(listenerContainer);
        this.messagingTemplate = messagingTemplate;
        this.seatSelectionService = seatSelectionService;
    }

    @Override
    public void onMessage(@NonNull Message message, byte[] pattern) {
        String expiredKey = message.toString();
        System.out.println("Expired key: " + expiredKey);

        if (expiredKey.startsWith("seat_locks:")) {
            try {
                String[] parts = expiredKey.split(":");
                if (parts.length == 3) {
                    Integer showtimeId = Integer.parseInt(parts[1]);
                    int seatId = Integer.parseInt(parts[2]);

                    seatSelectionService.releaseExpiredSeat(showtimeId, seatId);

                    Map<String, Object> response = Map.of("status", "released", "seatId", seatId);
                    messagingTemplate.convertAndSend("/topic/showtimes/" + showtimeId + "/seats", response);
                    System.out.println("Released seat " + seatId + " for showtime " + showtimeId);
                }
            } catch (Exception e) {
                System.err.println("Error processing expired key: " + expiredKey + " - " + e.getMessage());
            }
        }
    }
}
