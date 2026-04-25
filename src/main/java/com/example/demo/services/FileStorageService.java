package com.example.demo.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService() {
        // Thư mục lưu trữ là "uploads" tại thư mục gốc của dự án
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Kiểm tra tên file
        String originalFileName = org.springframework.util.StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Kiểm tra tên file không hợp lệ
            if(originalFileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + originalFileName);
            }

            // Kiểm tra kích thước file (ví dụ: giới hạn 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new RuntimeException("File size exceeds the limit of 5MB");
            }

            // Đổi tên file để tránh trùng lặp: UUID + đuôi file gốc
            String fileExtension = "";
            int i = originalFileName.lastIndexOf('.');
            if (i > 0) {
                fileExtension = originalFileName.substring(i);
            }
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // Copy file vào thư mục đích (ghi đè nếu trùng tên - dù UUID rất khó trùng)
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return newFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }

        // Nếu fileName là URL đầy đủ, chỉ lấy phần tên file ở cuối
        if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
            int lastSlashIndex = fileName.lastIndexOf('/');
            if (lastSlashIndex != -1) {
                fileName = fileName.substring(lastSlashIndex + 1);
            }
        }

        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            // Log lỗi nhưng không ném exception để không làm gián đoạn luồng chính
            System.err.println("Could not delete file: " + fileName);
        }
    }
}
