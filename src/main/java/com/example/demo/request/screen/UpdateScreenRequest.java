package com.example.demo.request.screen;

import com.example.demo.model.Screen;
import lombok.Data;

@Data
public class UpdateScreenRequest {
    private String name;
    private Screen.ScreenType screenType;
    private Integer totalSeats;
    private Boolean isActive;
}
