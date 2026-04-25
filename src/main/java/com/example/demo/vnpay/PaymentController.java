package com.example.demo.vnpay;

import com.example.demo.model.Booking;
import com.example.demo.request.CreatePaymentRequest;
import com.example.demo.response.PendingBookingDTO;
import com.example.demo.services.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/vnpay")
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayConfig vnPayConfig;
    private final BookingService bookingService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(
            @RequestBody CreatePaymentRequest paymentRequest,
            @AuthenticationPrincipal(expression = "id") Integer userId,
            HttpServletRequest request) {
        try {
            // 1. Tạo DTO ban đầu từ request của client
            PendingBookingDTO pendingBooking = PendingBookingDTO.builder()
                    .userId(userId)
                    .showtimeId(paymentRequest.getShowtimeId())
                    .seatIds(paymentRequest.getSeatIds())
                    .foodItems(paymentRequest.getFoodItems())
                    .promotionCode(paymentRequest.getPromotionCode())
                    .pointsUsed(paymentRequest.getPointsUsed())
                    .notes(paymentRequest.getNotes())
                    .build();

            // 2. Gọi service để tính toán và xác thực lại toàn bộ giá trị
            pendingBooking = bookingService.calculateBookingDetails(pendingBooking);

            BigDecimal finalAmount = pendingBooking.getFinalAmount();
            long amountInCent = finalAmount.multiply(new BigDecimal("100")).longValue();

            // 3. Tạo mã giao dịch và lưu "giỏ hàng" đã được xác thực vào Redis
            String vnp_TxnRef = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + VNPayUtil.getRandomNumber(6);
            String pendingBookingJson = objectMapper.writeValueAsString(pendingBooking);
            String redisKey = "pending_booking:" + vnp_TxnRef;
            redisTemplate.opsForValue().set(redisKey, pendingBookingJson, 15, TimeUnit.MINUTES);

            // 4. Tạo URL thanh toán VNPay
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnPayConfig.getVnpVersion());
            vnp_Params.put("vnp_Command", vnPayConfig.getVnpCommand());
            vnp_Params.put("vnp_TmnCode", vnPayConfig.getVnpTmnCode());
            vnp_Params.put("vnp_Amount", String.valueOf(amountInCent));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", "Thanh toan ve xem phim " + vnp_TxnRef);
            vnp_Params.put("vnp_OrderType", vnPayConfig.getOrderType());
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnpReturnUrl());
            vnp_Params.put("vnp_IpAddr", request.getRemoteAddr());

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));
            cld.add(Calendar.MINUTE, 15);
            vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            for (String fieldName : fieldNames) {
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString())).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (!fieldNames.get(fieldNames.size() - 1).equals(fieldName)) {
                        hashData.append('&');
                        query.append('&');
                    }
                }
            }
            String queryUrl = query.toString();
            String vnp_SecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getVnpHashSecret(), hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
            String paymentUrl = vnPayConfig.getVnpUrl() + "?" + queryUrl;

            return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<String> vnpayReturn(@RequestParam Map<String, String> params) {
        try {
            String vnp_TxnRef = params.get("vnp_TxnRef");
            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_SecureHash = params.get("vnp_SecureHash");

            params.remove("vnp_SecureHashType");
            params.remove("vnp_SecureHash");

            String signValue = VNPayUtil.hashAllFields(params);
            String calculatedHash = VNPayUtil.hmacSHA512(vnPayConfig.getVnpHashSecret(), signValue);

            // Tạo HTML redirect về app với Deep Link
            String htmlResponse;

            if (calculatedHash.equals(vnp_SecureHash)) {
                if ("00".equals(vnp_ResponseCode)) {
                    String redisKey = "pending_booking:" + vnp_TxnRef;
                    String pendingBookingJson = redisTemplate.opsForValue().get(redisKey);

                    if (pendingBookingJson != null) {
                        PendingBookingDTO pendingBooking = objectMapper.readValue(pendingBookingJson, PendingBookingDTO.class);
                        Booking newBooking = bookingService.createBookingFromPending(pendingBooking);
                        redisTemplate.delete(redisKey);

                        // Redirect về app với success
                        htmlResponse = generateRedirectHtml(
                                "Thanh toán thành công",
                                "Đặt vé thành công! Mã đặt vé: " + newBooking.getBookingCode(),
                                "success",
                                newBooking.getBookingCode()
                        );
                    } else {
                        htmlResponse = generateRedirectHtml(
                                "Lỗi",
                                "Giao dịch đã hết hạn hoặc đã được xử lý",
                                "error",
                                null
                        );
                    }
                } else {
                    htmlResponse = generateRedirectHtml(
                            "Thanh toán thất bại",
                            "Giao dịch không thành công. Vui lòng thử lại.",
                            "failed",
                            null
                    );
                }
            } else {
                htmlResponse = generateRedirectHtml(
                        "Lỗi bảo mật",
                        "Chữ ký không hợp lệ",
                        "error",
                        null
                );
            }

            return ResponseEntity.ok()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body(htmlResponse);

        } catch (Exception e) {
            String htmlResponse = generateRedirectHtml(
                    "Lỗi hệ thống",
                    "Đã xảy ra lỗi không xác định",
                    "error",
                    null
            );
            return ResponseEntity.ok()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body(htmlResponse);
        }
    }

    private String generateRedirectHtml(String title, String message, String status, String bookingCode) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>").append(title).append("</title>");
        html.append("<style>");
        html.append("body { font-family: -apple-system, system-ui, sans-serif; display: flex; align-items: center; justify-content: center; min-height: 100vh; margin: 0; padding: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }");
        html.append(".container { background: white; border-radius: 20px; padding: 40px; text-align: center; max-width: 400px; box-shadow: 0 20px 60px rgba(0,0,0,0.3); }");
        html.append(".icon { font-size: 64px; margin-bottom: 20px; }");
        html.append(".title { font-size: 24px; font-weight: bold; color: #1a1a1a; margin-bottom: 10px; }");
        html.append(".message { font-size: 16px; color: #666; margin-bottom: 20px; line-height: 1.5; }");
        html.append(".booking-code { background: #f3f4f6; padding: 12px; border-radius: 8px; font-weight: bold; font-size: 18px; color: #111827; margin: 20px 0; }");
        html.append(".spinner { border: 3px solid #f3f3f3; border-top: 3px solid #667eea; border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; margin: 20px auto; }");
        html.append("@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");

        if ("success".equals(status)) {
            html.append("<div class='icon'>✅</div>");
        } else if ("failed".equals(status)) {
            html.append("<div class='icon'>❌</div>");
        } else {
            html.append("<div class='icon'>⚠️</div>");
        }

        html.append("<div class='title'>").append(title).append("</div>");
        html.append("<div class='message'>").append(message).append("</div>");

        if (bookingCode != null) {
            html.append("<div class='booking-code'>").append(bookingCode).append("</div>");
        }

        html.append("<div class='spinner'></div>");
        html.append("<div style='color: #999; font-size: 14px; margin-top: 10px;'>Đang quay về ứng dụng...</div>");
        html.append("</div>");

        // Auto close WebView sau 2 giây
        html.append("<script>");
        html.append("setTimeout(function() {");
        html.append("  if (window.ReactNativeWebView) {");
        html.append("    window.ReactNativeWebView.postMessage(JSON.stringify({");
        html.append("      type: 'PAYMENT_RESULT',");
        html.append("      status: '").append(status).append("',");
        html.append("      bookingCode: ").append(bookingCode != null ? "'" + bookingCode + "'" : "null");
        html.append("    }));");
        html.append("  }");
        html.append("}, 2000);");
        html.append("</script>");

        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}