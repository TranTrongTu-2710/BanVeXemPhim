package com.example.demo.email;

import com.example.demo.model.Booking;
import com.example.demo.services.QrCodeService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final QrCodeService qrCodeService;

    @Async
    public void sendBookingConfirmationEmail(Booking booking) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

            // 1. Tạo mã QR
            byte[] qrCodeImage = qrCodeService.generateQrCodeImage(booking.getBookingCode(), 250, 250);

            // 2. Chuẩn bị context
            Context context = new Context();
            context.setVariable("booking", booking);
            context.setVariable("qrCodeCid", "bookingQrCode");

            // Danh sách ghế
            String seatNames = booking.getBookingSeats().stream()
                    .map(bs -> bs.getSeat().getRowName() + bs.getSeat().getSeatNumber())
                    .collect(Collectors.joining(", "));
            context.setVariable("seatNames", seatNames);

            // Danh sách đồ ăn (Mới)
            String foodList = "Không có";
            if (booking.getBookingFoods() != null && !booking.getBookingFoods().isEmpty()) {
                foodList = booking.getBookingFoods().stream()
                        .map(bf -> bf.getFoodItem().getName() + " x" + bf.getQuantity())
                        .collect(Collectors.joining(", "));
            }
            context.setVariable("foodList", foodList);

            // 3. Render template
            String htmlContent = templateEngine.process("booking-confirmation", context);

            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("Vé điện tử: " + booking.getShowtime().getMovie().getTitle() + " - " + booking.getBookingCode());
            helper.setText(htmlContent, true);

            // 4. Đính kèm ảnh QR
            helper.addInline("bookingQrCode", new ByteArrayResource(qrCodeImage), "image/png");

            mailSender.send(mimeMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
