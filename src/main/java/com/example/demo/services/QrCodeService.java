package com.example.demo.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class QrCodeService {

    /**
     * Tạo một mảng byte chứa hình ảnh mã QR từ một chuỗi dữ liệu.
     * @param text Dữ liệu cần mã hóa (ví dụ: bookingCode)
     * @param width Chiều rộng của ảnh QR
     * @param height Chiều cao của ảnh QR
     * @return Mảng byte của hình ảnh PNG
     * @throws WriterException nếu có lỗi khi tạo mã QR
     * @throws IOException nếu có lỗi khi ghi vào stream
     */
    public byte[] generateQrCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        
        return pngOutputStream.toByteArray();
    }
}
