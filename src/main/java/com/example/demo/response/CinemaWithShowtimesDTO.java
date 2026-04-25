package com.example.demo.response;

import com.example.demo.model.Cinema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CinemaWithShowtimesDTO {
    private Integer id;
    private String name;
    private String address;
    private String city;
    private String district;
    private List<ShowtimeResponseDTO> showtimes;

    /**
     * Constructor này nhận vào một Cinema entity và một movieId,
     * sau đó tự lọc ra các suất chiếu phù hợp.
     */
    public CinemaWithShowtimesDTO(Cinema cinema, Integer movieId) {
        this.id = cinema.getId();
        this.name = cinema.getName();
        this.address = cinema.getAddress();
        this.city = cinema.getCity();
        this.district = cinema.getDistrict();

        if (cinema.getScreens() != null) {
            LocalDate today = LocalDate.now();
            // Lấy tất cả suất chiếu từ tất cả các phòng của rạp này
            this.showtimes = cinema.getScreens().stream()
                    .flatMap(screen -> screen.getShowtimes().stream())
                    // Lọc những suất chiếu của đúng phim đang tìm VÀ chưa hết hạn
                    .filter(showtime -> showtime.getMovie().getId().equals(movieId) && !showtime.getShowDate().isBefore(today))
                    .map(ShowtimeResponseDTO::new) // Chuyển đổi sang DTO
                    .collect(Collectors.toList());
        }
    }
}
