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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/vnpay")
@RequiredArgsConstructor
public class VNPayController {

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
            PendingBookingDTO pendingBooking = bookingService.calculateBookingDetails(
                PendingBookingDTO.builder()
                    .userId(userId)
                    .showtimeId(paymentRequest.getShowtimeId())
                    .seatIds(paymentRequest.getSeatIds())
                    .foodItems(paymentRequest.getFoodItems())
                    .promotionCode(paymentRequest.getPromotionCode())
                    .pointsUsed(paymentRequest.getPointsUsed())
                    .notes(paymentRequest.getNotes())
                    .build()
            );

            long amountInCent = pendingBooking.getFinalAmount().multiply(new BigDecimal("100")).longValue();

            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            
            String vnp_CreateDate = now.format(formatter);
            String vnp_TxnRef = vnp_CreateDate + VNPayUtil.getRandomNumber(6);

            redisTemplate.opsForValue().set("pending_booking:" + vnp_TxnRef, objectMapper.writeValueAsString(pendingBooking), 15, TimeUnit.MINUTES);

            Map<String, String> vnp_Params = new TreeMap<>();
            vnp_Params.put("vnp_Version", vnPayConfig.getVnpVersion());
            vnp_Params.put("vnp_Command", vnPayConfig.getVnpCommand());
            vnp_Params.put("vnp_TmnCode", vnPayConfig.getVnpTmnCode().trim());
            vnp_Params.put("vnp_Amount", String.valueOf(amountInCent));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            
            // Đơn giản hóa OrderInfo
            String orderInfo = "Thanh toan " + vnp_TxnRef;
            vnp_Params.put("vnp_OrderInfo", orderInfo);
            
            vnp_Params.put("vnp_OrderType", vnPayConfig.getOrderType());
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnpReturnUrl());
            
            String ipAddr = VNPayUtil.getIpAddress(request);
            if ("0:0:0:0:0:0:0:1".equals(ipAddr)) {
                ipAddr = "127.0.0.1";
            }
            vnp_Params.put("vnp_IpAddr", ipAddr);
            
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            vnp_Params.put("vnp_ExpireDate", now.plusMinutes(15).format(formatter));

            List<String> queryParams = new ArrayList<>();
            StringBuilder hashData = new StringBuilder();

            for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue();
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    // Build hash data
                    if (hashData.length() > 0) {
                        hashData.append('&');
                    }
                    hashData.append(fieldName);
                    hashData.append('=');
                    // Encode chuẩn Java (Space -> +) theo tài liệu VNPay
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                    
                    // Build query params
                    queryParams.add(URLEncoder.encode(fieldName, StandardCharsets.UTF_8) + "=" + URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                }
            }

            String queryUrl = String.join("&", queryParams);
            String vnp_SecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getVnpHashSecret().trim(), hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
            String paymentUrl = vnPayConfig.getVnpUrl() + "?" + queryUrl;

            return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<String> vnpayReturn(HttpServletRequest request) {
        try {
            // 1. Lấy Raw Query String để xử lý chính xác nhất
            String queryString = request.getQueryString();
            System.out.println("--------------------------------------------------");
            System.out.println("VNPAY RETURN RAW QUERY: " + queryString);

            // 2. Parse thủ công để lấy params và giữ nguyên encoding
            Map<String, String> params = new TreeMap<>();
            String vnp_SecureHash = "";
            String vnp_ResponseCode = "";
            String vnp_TxnRef = "";

            if (queryString != null) {
                String[] pairs = queryString.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    if (idx > 0) {
                        // Decode Key để sắp xếp đúng
                        String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                        // GIỮ NGUYÊN VALUE (Raw Value)
                        String value = pair.substring(idx + 1);

                        if ("vnp_SecureHash".equals(key)) {
                            vnp_SecureHash = value;
                        } else if ("vnp_SecureHashType".equals(key)) {
                            // Bỏ qua
                        } else {
                            params.put(key, value);
                            
                            // Decode giá trị cần dùng để xử lý logic
                            if ("vnp_ResponseCode".equals(key)) {
                                vnp_ResponseCode = URLDecoder.decode(value, StandardCharsets.UTF_8);
                            } else if ("vnp_TxnRef".equals(key)) {
                                vnp_TxnRef = URLDecoder.decode(value, StandardCharsets.UTF_8);
                            }
                        }
                    }
                }
            }

            // 3. Tạo chuỗi hash data từ Raw Values
            StringBuilder hashData = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue();
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    if (hashData.length() > 0) {
                        hashData.append('&');
                    }
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(fieldValue); // Dùng trực tiếp giá trị raw từ VNPay
                }
            }

            String signValue = hashData.toString();
            String calculatedHash = VNPayUtil.hmacSHA512(vnPayConfig.getVnpHashSecret().trim(), signValue);

            // LOG DEBUG
            System.out.println("Sign Value (Raw): " + signValue);
            System.out.println("Received Hash: " + vnp_SecureHash);
            System.out.println("Calculated Hash: " + calculatedHash);
            System.out.println("--------------------------------------------------");

            String htmlResponse;

            if (calculatedHash.equals(vnp_SecureHash)) {
                if ("00".equals(vnp_ResponseCode)) {
                    String redisKey = "pending_booking:" + vnp_TxnRef;
                    String pendingBookingJson = redisTemplate.opsForValue().get(redisKey);

                    if (pendingBookingJson != null) {
                        PendingBookingDTO pendingBooking = objectMapper.readValue(pendingBookingJson, PendingBookingDTO.class);
                        Booking newBooking = bookingService.createBookingFromPending(pendingBooking);
                        redisTemplate.delete(redisKey);
                        htmlResponse = generateRedirectHtml("Thanh toán thành công", "Mã vé: " + newBooking.getBookingCode(), "success", newBooking.getBookingCode());
                    } else {
                        htmlResponse = generateRedirectHtml("Lỗi", "Giao dịch hết hạn hoặc đã xử lý", "error", null);
                    }
                } else {
                    htmlResponse = generateRedirectHtml("Thanh toán thất bại", "Lỗi từ ngân hàng", "failed", null);
                }
            } else {
                htmlResponse = generateRedirectHtml("Lỗi bảo mật", "Sai chữ ký", "error", null);
            }

            return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8").body(htmlResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok().body("System Error: " + e.getMessage());
        }
    }

    private String generateRedirectHtml(String title, String message, String status, String bookingCode) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'><title>").append(title).append("</title>");
        html.append("<style>body{font-family:-apple-system,system-ui,sans-serif;display:flex;align-items:center;justify-content:center;min-height:100vh;margin:0;padding:20px;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%)}.container{background:white;border-radius:20px;padding:40px;text-align:center;max-width:400px;box-shadow:0 20px 60px rgba(0,0,0,.3)}.icon{font-size:64px;margin-bottom:20px}.title{font-size:24px;font-weight:bold;color:#1a1a1a;margin-bottom:10px}.message{font-size:16px;color:#666;margin-bottom:20px;line-height:1.5}.booking-code{background:#f3f4f6;padding:12px;border-radius:8px;font-weight:bold;font-size:18px;color:#111827;margin:20px 0}.spinner{border:3px solid #f3f3f3;border-top:3px solid #667eea;border-radius:50%;width:40px;height:40px;animation:spin 1s linear infinite;margin:20px auto}@keyframes spin{0%{transform:rotate(0)}100%{transform:rotate(360deg)}}</style>");
        html.append("</head><body><div class='container'>");
        if("success".equals(status)){html.append("<div class='icon'>✅</div>");}else if("failed".equals(status)){html.append("<div class='icon'>❌</div>");}else{html.append("<div class='icon'>⚠️</div>");}
        html.append("<div class='title'>").append(title).append("</div><div class='message'>").append(message).append("</div>");
        if(bookingCode!=null){html.append("<div class='booking-code'>").append(bookingCode).append("</div>");}
        html.append("<div class='spinner'></div><div style='color:#999;font-size:14px;margin-top:10px'>Đang quay về ứng dụng...</div></div>");
        html.append("<script>setTimeout(function(){if(window.ReactNativeWebView){window.ReactNativeWebView.postMessage(JSON.stringify({type:'PAYMENT_RESULT',status:'").append(status).append("',bookingCode:").append(bookingCode!=null?"'"+bookingCode+"'":"null").append("}));}},2000);</script>");
        html.append("</body></html>");
        return html.toString();
    }
}
