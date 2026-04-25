package com.example.demo.config;

import com.example.demo.config.SecurityConfig.JwtService;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Chỉ xác thực khi client gửi lệnh CONNECT
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Lấy token từ header 'Authorization' của STOMP
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            log.info("Authorization header: {}", authHeader);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    Integer userId = jwtService.verifyAndGetUserId(token);
                    Optional<User> userOptional = userRepository.findById(userId);

                    if (userOptional.isPresent()) {
                        User user = userOptional.get();
                        // Tạo đối tượng xác thực
                        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name().toUpperCase(Locale.ROOT)));
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                user, // Principal bây giờ là đối tượng User
                                null,
                                authorities
                        );
                        // Thiết lập Principal cho session WebSocket này
                        accessor.setUser(authentication);
                        log.info("Authenticated WebSocket user: {}", user.getEmail());
                    }
                } catch (Exception e) {
                    log.error("WebSocket authentication failed: {}", e.getMessage());
                    // Có thể từ chối kết nối ở đây nếu cần
                }
            }
        }
        return message;
    }
}
