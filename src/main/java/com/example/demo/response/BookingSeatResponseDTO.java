package com.example.demo.response;

import com.example.demo.model.BookingSeat;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookingSeatResponseDTO {
    private SeatResponseDTO seat;
    private BigDecimal price;

    public BookingSeatResponseDTO(BookingSeat bookingSeat) {
        if (bookingSeat.getSeat() != null) {
            this.seat = new SeatResponseDTO(bookingSeat.getSeat());
        }
        this.price = bookingSeat.getPrice();
    }
}
