package com.example.demo.response;

import com.example.demo.model.Screen;
import lombok.Data;

import java.io.Serializable;

@Data
public class ScreenResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private Screen.ScreenType screenType;
    private Integer totalSeats;

    public ScreenResponseDTO(Screen screen) {
        this.id = screen.getId();
        this.name = screen.getName();
        this.screenType = screen.getScreenType();
        this.totalSeats = screen.getTotalSeats();
    }
}
