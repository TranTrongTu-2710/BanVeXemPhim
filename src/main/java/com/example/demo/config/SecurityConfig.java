package com.example.demo.config;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain api(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        /* =================== STATIC RESOURCES =================== */
                        .requestMatchers("/uploads/**").permitAll()

                        /* =================== WEBSOCKET =================== */
                        .requestMatchers("/ws/**").permitAll()

                        /* =================== BANNERS =================== */
                        .requestMatchers(HttpMethod.GET, "/banners").permitAll()
                        .requestMatchers(HttpMethod.GET, "/banners/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/banners").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/banners/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/banners/**").hasRole("ADMIN")

                        /* =================== USERS =================== */
                        .requestMatchers(HttpMethod.POST, "/users", "/users/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/logout").authenticated()
                        .requestMatchers(HttpMethod.GET,  "/users/me").authenticated()
                        .requestMatchers(HttpMethod.PATCH,"/users/me").authenticated()
                        .requestMatchers(HttpMethod.GET,  "/users", "/users/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.PUT,"/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,"/users/**").hasRole("ADMIN")

                        /* =================== CINEMAS =================== */
                        .requestMatchers(HttpMethod.GET, "/cinemas", "/cinemas/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/cinemas").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/cinemas/all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/cinemas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/cinemas/**").hasRole("ADMIN")

                        /* =================== MOVIES =================== */
                        .requestMatchers(HttpMethod.GET, "/movies", "/movies/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/movies/staff/scheduled-today").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/movies").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/movies/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/movies/**").hasRole("ADMIN")

                        /* =================== FOODS =================== */
                        .requestMatchers(HttpMethod.GET, "/foods", "/foods/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/foods").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.GET, "/foods/all").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.PUT, "/foods/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.DELETE, "/foods/**").hasAnyRole("ADMIN", "STAFF")
                        
                        /* =================== FOOD ORDERS (AT COUNTER) =================== */
                        .requestMatchers("/food-orders/**").hasAnyRole("ADMIN", "STAFF")

                        /* =================== PROMOTIONS =================== */
                        .requestMatchers(HttpMethod.GET, "/promotions/public", "/promotions/code/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/promotions").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/promotions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/promotions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/promotions/**").hasRole("ADMIN")

                        /* =================== SCREENS, SEATS, SHOWTIMES, SEAT PRICES =================== */
                        .requestMatchers(HttpMethod.GET, "/screens/**", "/seats/**", "/showtimes/**", "/seat-prices/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/screens", "/seats", "/showtimes", "/seat-prices").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/screens/**", "/seats/**", "/showtimes/**", "/seat-prices/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/screens/**", "/seats/**", "/showtimes/**", "/seat-prices/**").hasRole("ADMIN") // Thêm PATCH
                        .requestMatchers(HttpMethod.DELETE, "/screens/**", "/seats/**", "/showtimes/**", "/seat-prices/**").hasRole("ADMIN")

                        /* =================== BOOKINGS =================== */
                        .requestMatchers(HttpMethod.POST, "/bookings").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/bookings/staff/create").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/bookings/check-in").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/bookings/my-bookings").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/bookings", "/bookings/code/**", "/bookings/{id}").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.PATCH, "/bookings/{id}/cancel").authenticated()

                        /* =================== REVIEWS =================== */
                        .requestMatchers(HttpMethod.GET, "/reviews/movie/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/reviews").authenticated()
                        .requestMatchers(HttpMethod.GET, "/reviews/my-reviews").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/reviews/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/reviews/**").hasRole("ADMIN")

                        /* =================== NOTIFICATIONS =================== */
                        .requestMatchers(HttpMethod.POST, "/notifications/broadcast").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/notifications/system").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/notifications/**").hasRole("ADMIN") // Sửa
                        .requestMatchers(HttpMethod.DELETE, "/notifications/**").hasRole("ADMIN") // Xóa
                        .requestMatchers("/notifications/**").authenticated()

                        /* =================== PAYMENT TRANSACTIONS =================== */
                        .requestMatchers("/payment-transactions/**").hasRole("ADMIN")

                        /* =================== DASHBOARD =================== */
                        .requestMatchers("/dashboard/**").hasRole("ADMIN")

                        /* =================== Fallback =================== */
                        .anyRequest().permitAll()
                )
                .httpBasic(Customizer.withDefaults());

        http.addFilterBefore(jwtAuthFilter, BasicAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager() { return authentication -> authentication; }

    /* ================= JWT ================= */

    @Component
    public static class JwtService {
        private final SecretKey key;

        public JwtService(@Value("${jwt.secret}") String secret) {
            if (secret == null || secret.isBlank()) throw new IllegalArgumentException("Missing jwt.secret");
            byte[] kb = secret.startsWith("base64:")
                    ? Decoders.BASE64.decode(secret.substring(7))
                    : secret.getBytes(StandardCharsets.UTF_8);
            if (kb.length < 32) throw new IllegalArgumentException("jwt.secret must be >= 32 bytes (HS256).");
            this.key = Keys.hmacShaKeyFor(kb);
        }

        public String sign(Integer userId) {
            long now = System.currentTimeMillis();
            return Jwts.builder()
                    .claim("_id", userId)
                    .setIssuedAt(new Date(now))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        }

        public Integer verifyAndGetUserId(String token) {
            Claims c = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();
            return c.get("_id", Integer.class);
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class JwtAuthFilter extends OncePerRequestFilter {
        private final JwtService jwtService;
        private final UserRepository userRepo;

        @Override
        protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                throws ServletException, IOException {
            String auth = req.getHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                chain.doFilter(req, res);
                return;
            }
            String token = auth.substring(7);
            try {
                Integer userId = jwtService.verifyAndGetUserId(token);
                Optional<User> ou = userRepo.findById(userId);

                if (ou.isEmpty() || !ou.get().getIsActive() || !ou.get().getTokens().contains(token)) {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json");
                    res.getWriter().write("{\"error\":\"Please authenticate.\"}");
                    return;
                }
                User u = ou.get();
                req.setAttribute("currentUser", u);
                req.setAttribute("currentToken", token);

                Collection<GrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name().toUpperCase(Locale.ROOT)));
                var up = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        u, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(up);

            } catch (JwtException | IllegalArgumentException ex) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"Please authenticate.\"}");
                return;
            }
            chain.doFilter(req, res);
        }
    }
}
