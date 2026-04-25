package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cấu hình để phục vụ file từ thư mục "uploads"
        // Khi truy cập http://localhost:8080/uploads/abc.jpg -> sẽ tìm file trong thư mục uploads/ ở gốc dự án
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
    
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Cho phép trailing slash (ví dụ /movies/27/ cũng được chấp nhận như /movies/27)
        configurer.setUseTrailingSlashMatch(true);
    }
}
