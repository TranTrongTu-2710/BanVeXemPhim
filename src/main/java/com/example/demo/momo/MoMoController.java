package com.example.demo.momo;

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
import org.springframework.web.client.RestTemplate;

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
@RequestMapping("/api/momo")
@RequiredArgsConstructor
public class MoMoController {

    private final MoMoConfig moMoConfig;
    private final BookingService bookingService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

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

            long amount = pendingBooking.getFinalAmount().longValue();

            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            
            String orderId = now.format(formatter) + UUID.randomUUID().toString().substring(0, 6);
            String requestId = UUID.randomUUID().toString();

            redisTemplate.opsForValue().set("pending_booking:" + orderId, objectMapper.writeValueAsString(pendingBooking), 15, TimeUnit.MINUTES);

            String orderInfo = "Thanh toan don hang " + orderId;
            String redirectUrl = moMoConfig.getReturnUrl();
            String ipnUrl = moMoConfig.getNotifyUrl();
            String requestType = "captureWallet";
            String extraData = "";

            // Trim các giá trị config để tránh lỗi khoảng trắng
            String accessKey = moMoConfig.getAccessKey().trim();
            String partnerCode = moMoConfig.getPartnerCode().trim();
            String secretKey = moMoConfig.getSecretKey().trim();

            String rawSignature = "accessKey=" + accessKey +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + ipnUrl +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + partnerCode +
                    "&redirectUrl=" + redirectUrl +
                    "&requestId=" + requestId +
                    "&requestType=" + requestType;

            System.out.println("--------------------------------------------------");
            System.out.println("MOMO RAW SIGNATURE: " + rawSignature);
            System.out.println("--------------------------------------------------");

            String signature = MoMoUtil.hmacSHA256(rawSignature, secretKey);

            MoMoPaymentRequest moMoRequest = MoMoPaymentRequest.builder()
                    .partnerCode(partnerCode)
                    .requestId(requestId)
                    .amount(String.valueOf(amount))
                    .orderId(orderId)
                    .orderInfo(orderInfo)
                    .redirectUrl(redirectUrl)
                    .ipnUrl(ipnUrl)
                    .requestType(requestType)
                    .extraData(extraData)
                    .signature(signature)
                    .lang("vi")
                    .build();

            MoMoPaymentResponse response = restTemplate.postForObject(moMoConfig.getEndpoint(), moMoRequest, MoMoPaymentResponse.class);

            return ResponseEntity.ok(Map.of("paymentUrl", response.getPayUrl()));

        } catch (Exception e) {
            e.printStackTrace();
            // Trả về lỗi chi tiết hơn nếu là lỗi từ MoMo
            if (e instanceof org.springframework.web.client.HttpClientErrorException) {
                return ResponseEntity.status(((org.springframework.web.client.HttpClientErrorException) e).getStatusCode())
                        .body(Map.of("error", ((org.springframework.web.client.HttpClientErrorException) e).getResponseBodyAsString()));
            }
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/return")
    public ResponseEntity<String> momoReturn(@RequestParam Map<String, String> params) {
        try {
            String orderId = params.get("orderId");
            String resultCode = params.get("resultCode");
            String message = params.get("message");

            String htmlResponse;

            if ("0".equals(resultCode)) {
                String redisKey = "pending_booking:" + orderId;
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
                htmlResponse = generateRedirectHtml("Thanh toán thất bại", message, "failed", null);
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
