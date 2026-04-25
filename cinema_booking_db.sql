-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th10 19, 2025 lúc 03:46 PM
-- Phiên bản máy phục vụ: 10.4.32-MariaDB
-- Phiên bản PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `cinema_booking_db`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `bookings`
--

CREATE TABLE `bookings` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `showtime_id` int(11) NOT NULL,
  `booking_code` varchar(20) NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `discount_amount` decimal(10,2) DEFAULT 0.00,
  `final_amount` decimal(10,2) NOT NULL,
  `payment_method` enum('cash','momo','zalopay','vnpay','credit_card') NOT NULL,
  `payment_status` enum('pending','paid','failed','refunded') DEFAULT 'pending',
  `booking_status` enum('pending','confirmed','cancelled','completed') DEFAULT 'pending',
  `points_earned` int(11) DEFAULT 0,
  `points_used` int(11) DEFAULT 0,
  `booking_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `payment_date` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `qr_code` varchar(500) DEFAULT NULL COMMENT 'QR code cho vé',
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `booking_food`
--

CREATE TABLE `booking_food` (
  `id` int(11) NOT NULL,
  `booking_id` int(11) NOT NULL,
  `food_item_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `booking_seats`
--

CREATE TABLE `booking_seats` (
  `id` int(11) NOT NULL,
  `booking_id` int(11) NOT NULL,
  `seat_id` int(11) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `cinemas`
--

CREATE TABLE `cinemas` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `address` text NOT NULL,
  `city` varchar(100) NOT NULL,
  `district` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `latitude` decimal(10,8) DEFAULT NULL,
  `longitude` decimal(11,8) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `facilities` text DEFAULT NULL COMMENT 'JSON: parking, restaurant, etc',
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `food_items`
--

CREATE TABLE `food_items` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `category` enum('combo','popcorn','drink','snack') NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `image_url` varchar(500) DEFAULT NULL,
  `is_available` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `movies`
--

CREATE TABLE `movies` (
  `id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `original_title` varchar(255) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `duration` int(11) NOT NULL COMMENT 'Thời lượng (phút)',
  `release_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `director` varchar(255) DEFAULT NULL,
  `cast` text DEFAULT NULL COMMENT 'JSON array of actors',
  `genres` text DEFAULT NULL COMMENT 'JSON array: action, comedy, etc',
  `language` varchar(255) DEFAULT NULL,
  `subtitle` varchar(255) DEFAULT NULL,
  `rating` enum('P','C13','C16','C18') NOT NULL COMMENT 'Phân loại độ tuổi',
  `poster_url` varchar(500) DEFAULT NULL,
  `trailer_url` varchar(500) DEFAULT NULL,
  `banner_url` varchar(500) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `status` enum('coming_soon','now_showing','ended') DEFAULT 'coming_soon',
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `notifications`
--

CREATE TABLE `notifications` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `type` enum('booking','promotion','reminder','system') NOT NULL,
  `title` varchar(255) NOT NULL,
  `message` text NOT NULL,
  `is_read` tinyint(1) DEFAULT 0,
  `related_id` int(11) DEFAULT NULL COMMENT 'ID liên quan (booking_id, promotion_id...)',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `payment_transactions`
--

CREATE TABLE `payment_transactions` (
  `id` int(11) NOT NULL,
  `booking_id` int(11) NOT NULL,
  `transaction_id` varchar(255) NOT NULL,
  `payment_method` enum('cash','momo','zalopay','vnpay','credit_card') NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `status` enum('pending','success','failed','refunded') NOT NULL,
  `payment_data` text DEFAULT NULL COMMENT 'JSON response từ payment gateway',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `promotions`
--

CREATE TABLE `promotions` (
  `id` int(11) NOT NULL,
  `code` varchar(50) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `discount_type` enum('percentage','fixed_amount','points') NOT NULL,
  `discount_value` decimal(10,2) NOT NULL,
  `min_purchase_amount` decimal(10,2) DEFAULT 0.00,
  `max_discount_amount` decimal(10,2) DEFAULT NULL,
  `usage_limit` int(11) DEFAULT NULL COMMENT 'Số lần sử dụng tối đa',
  `used_count` int(11) DEFAULT 0,
  `valid_from` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `valid_to` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `applicable_to` enum('all','movie','food','booking') DEFAULT 'all',
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `reviews`
--

CREATE TABLE `reviews` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `movie_id` int(11) NOT NULL,
  `booking_id` int(11) DEFAULT NULL,
  `rating` int(11) NOT NULL COMMENT '1-5 sao',
  `comment` text DEFAULT NULL,
  `is_approved` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `screens`
--

CREATE TABLE `screens` (
  `id` int(11) NOT NULL,
  `cinema_id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL COMMENT 'Tên phòng: Screen 1, 2, 3...',
  `screen_type` enum('2D','3D','IMAX','4DX','Dolby Atmos') NOT NULL,
  `total_seats` int(11) NOT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `seats`
--

CREATE TABLE `seats` (
  `id` int(11) NOT NULL,
  `screen_id` int(11) NOT NULL,
  `row_name` varchar(5) NOT NULL COMMENT 'A, B, C...',
  `seat_number` int(11) NOT NULL COMMENT '1, 2, 3...',
  `seat_type` enum('standard','vip','couple') NOT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `seat_holds`
--

CREATE TABLE `seat_holds` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `showtime_id` int(11) NOT NULL,
  `seat_id` int(11) NOT NULL,
  `hold_until` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT 'Giữ ghế trong 10 phút',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `seat_prices`
--

CREATE TABLE `seat_prices` (
  `id` int(11) NOT NULL,
  `showtime_id` int(11) NOT NULL,
  `seat_type` enum('standard','vip','couple') NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `day_type` enum('weekday','weekend','holiday') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `showtimes`
--

CREATE TABLE `showtimes` (
  `id` int(11) NOT NULL,
  `movie_id` int(11) NOT NULL,
  `screen_id` int(11) NOT NULL,
  `show_date` date NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `base_price` decimal(10,2) NOT NULL,
  `status` enum('scheduled','selling','sold_out','cancelled') DEFAULT 'scheduled',
  `available_seats` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `gender` enum('male','female','other') DEFAULT NULL,
  `avatar_url` varchar(500) DEFAULT NULL,
  `role` enum('customer','admin','staff') DEFAULT 'customer',
  `points` int(11) DEFAULT 0 COMMENT 'Điểm tích lũy',
  `membership_tier` enum('silver','gold','platinum') DEFAULT 'silver',
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `users`
--

INSERT INTO `users` (`id`, `email`, `password_hash`, `full_name`, `phone`, `date_of_birth`, `gender`, `avatar_url`, `role`, `points`, `membership_tier`, `is_active`, `created_at`, `updated_at`) VALUES
(1, 'staff@gmail.com', '$2a$10$cEc4OEqcP2KNZIcDjLp5ueb9G4KRWvkeS8jFoKcmK1Ky33sWxRZhC', 'Trần Trọng Tú', '0123456789', NULL, NULL, NULL, 'staff', 0, NULL, 1, '2025-11-19 12:48:26', '2025-11-19 12:51:49'),
(2, 'admin@gmail.com', '$2a$10$DiAuCLsxTtHpBOG78EYTneupkmfEDQvCUDZAhcKdrkiKw5.tvnrRS', 'Trần Trọng Tú', '0123456780', NULL, NULL, NULL, 'admin', 0, NULL, 1, '2025-11-19 12:50:07', '2025-11-19 12:52:25');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `user_promotions`
--

CREATE TABLE `user_promotions` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `promotion_id` int(11) NOT NULL,
  `used_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `booking_id` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `bookings`
--
ALTER TABLE `bookings`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `booking_code` (`booking_code`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `showtime_id` (`showtime_id`);

--
-- Chỉ mục cho bảng `booking_food`
--
ALTER TABLE `booking_food`
  ADD PRIMARY KEY (`id`),
  ADD KEY `booking_id` (`booking_id`),
  ADD KEY `food_item_id` (`food_item_id`);

--
-- Chỉ mục cho bảng `booking_seats`
--
ALTER TABLE `booking_seats`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_booking_seat` (`booking_id`,`seat_id`),
  ADD UNIQUE KEY `UK39cm6toex08wkdr5sui1bid5b` (`booking_id`,`seat_id`),
  ADD KEY `seat_id` (`seat_id`);

--
-- Chỉ mục cho bảng `cinemas`
--
ALTER TABLE `cinemas`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `food_items`
--
ALTER TABLE `food_items`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `movies`
--
ALTER TABLE `movies`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Chỉ mục cho bảng `payment_transactions`
--
ALTER TABLE `payment_transactions`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `transaction_id` (`transaction_id`),
  ADD KEY `booking_id` (`booking_id`);

--
-- Chỉ mục cho bảng `promotions`
--
ALTER TABLE `promotions`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `code` (`code`);

--
-- Chỉ mục cho bảng `reviews`
--
ALTER TABLE `reviews`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_review` (`user_id`,`movie_id`),
  ADD UNIQUE KEY `UKovijrmb7g6cvqvlgr25vb8r98` (`user_id`,`movie_id`),
  ADD KEY `movie_id` (`movie_id`),
  ADD KEY `booking_id` (`booking_id`);

--
-- Chỉ mục cho bảng `screens`
--
ALTER TABLE `screens`
  ADD PRIMARY KEY (`id`),
  ADD KEY `cinema_id` (`cinema_id`);

--
-- Chỉ mục cho bảng `seats`
--
ALTER TABLE `seats`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_seat` (`screen_id`,`row_name`,`seat_number`),
  ADD UNIQUE KEY `UKdvnerjkwaaor0nrbkuaegafql` (`screen_id`,`row_name`,`seat_number`);

--
-- Chỉ mục cho bảng `seat_holds`
--
ALTER TABLE `seat_holds`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_seat_hold` (`showtime_id`,`seat_id`),
  ADD UNIQUE KEY `UKcytm5sj9sj1sgf56lpuyp6b9k` (`showtime_id`,`seat_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `seat_id` (`seat_id`);

--
-- Chỉ mục cho bảng `seat_prices`
--
ALTER TABLE `seat_prices`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_seat_price` (`showtime_id`,`seat_type`,`day_type`),
  ADD UNIQUE KEY `UKoodgup2rgv5r95tfrwwpvu28b` (`showtime_id`,`seat_type`,`day_type`);

--
-- Chỉ mục cho bảng `showtimes`
--
ALTER TABLE `showtimes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_showtime` (`screen_id`,`show_date`,`start_time`),
  ADD UNIQUE KEY `UKhb3ld2t7y0t7ni2wn8otxd9cg` (`screen_id`,`show_date`,`start_time`),
  ADD KEY `movie_id` (`movie_id`);

--
-- Chỉ mục cho bảng `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Chỉ mục cho bảng `user_promotions`
--
ALTER TABLE `user_promotions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `promotion_id` (`promotion_id`),
  ADD KEY `booking_id` (`booking_id`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `bookings`
--
ALTER TABLE `bookings`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `booking_food`
--
ALTER TABLE `booking_food`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `booking_seats`
--
ALTER TABLE `booking_seats`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `cinemas`
--
ALTER TABLE `cinemas`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `food_items`
--
ALTER TABLE `food_items`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `movies`
--
ALTER TABLE `movies`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `payment_transactions`
--
ALTER TABLE `payment_transactions`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `promotions`
--
ALTER TABLE `promotions`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `reviews`
--
ALTER TABLE `reviews`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `screens`
--
ALTER TABLE `screens`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `seats`
--
ALTER TABLE `seats`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `seat_holds`
--
ALTER TABLE `seat_holds`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `seat_prices`
--
ALTER TABLE `seat_prices`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `showtimes`
--
ALTER TABLE `showtimes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT cho bảng `user_promotions`
--
ALTER TABLE `user_promotions`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `bookings`
--
ALTER TABLE `bookings`
  ADD CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `bookings_ibfk_2` FOREIGN KEY (`showtime_id`) REFERENCES `showtimes` (`id`);

--
-- Các ràng buộc cho bảng `booking_food`
--
ALTER TABLE `booking_food`
  ADD CONSTRAINT `booking_food_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`),
  ADD CONSTRAINT `booking_food_ibfk_2` FOREIGN KEY (`food_item_id`) REFERENCES `food_items` (`id`);

--
-- Các ràng buộc cho bảng `booking_seats`
--
ALTER TABLE `booking_seats`
  ADD CONSTRAINT `booking_seats_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`),
  ADD CONSTRAINT `booking_seats_ibfk_2` FOREIGN KEY (`seat_id`) REFERENCES `seats` (`id`);

--
-- Các ràng buộc cho bảng `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Các ràng buộc cho bảng `payment_transactions`
--
ALTER TABLE `payment_transactions`
  ADD CONSTRAINT `payment_transactions_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`);

--
-- Các ràng buộc cho bảng `reviews`
--
ALTER TABLE `reviews`
  ADD CONSTRAINT `reviews_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `reviews_ibfk_2` FOREIGN KEY (`movie_id`) REFERENCES `movies` (`id`),
  ADD CONSTRAINT `reviews_ibfk_3` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`);

--
-- Các ràng buộc cho bảng `screens`
--
ALTER TABLE `screens`
  ADD CONSTRAINT `screens_ibfk_1` FOREIGN KEY (`cinema_id`) REFERENCES `cinemas` (`id`);

--
-- Các ràng buộc cho bảng `seats`
--
ALTER TABLE `seats`
  ADD CONSTRAINT `seats_ibfk_1` FOREIGN KEY (`screen_id`) REFERENCES `screens` (`id`);

--
-- Các ràng buộc cho bảng `seat_holds`
--
ALTER TABLE `seat_holds`
  ADD CONSTRAINT `seat_holds_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `seat_holds_ibfk_2` FOREIGN KEY (`showtime_id`) REFERENCES `showtimes` (`id`),
  ADD CONSTRAINT `seat_holds_ibfk_3` FOREIGN KEY (`seat_id`) REFERENCES `seats` (`id`);

--
-- Các ràng buộc cho bảng `seat_prices`
--
ALTER TABLE `seat_prices`
  ADD CONSTRAINT `seat_prices_ibfk_1` FOREIGN KEY (`showtime_id`) REFERENCES `showtimes` (`id`);

--
-- Các ràng buộc cho bảng `showtimes`
--
ALTER TABLE `showtimes`
  ADD CONSTRAINT `showtimes_ibfk_1` FOREIGN KEY (`movie_id`) REFERENCES `movies` (`id`),
  ADD CONSTRAINT `showtimes_ibfk_2` FOREIGN KEY (`screen_id`) REFERENCES `screens` (`id`);

--
-- Các ràng buộc cho bảng `user_promotions`
--
ALTER TABLE `user_promotions`
  ADD CONSTRAINT `user_promotions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `user_promotions_ibfk_2` FOREIGN KEY (`promotion_id`) REFERENCES `promotions` (`id`),
  ADD CONSTRAINT `user_promotions_ibfk_3` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
