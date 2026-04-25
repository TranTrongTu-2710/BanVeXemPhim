package com.example.demo.request.screen;

import com.example.demo.model.Screen;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateScreenRequest {
    @NotNull
    private Integer cinemaId;
    @NotBlank
    private String name;
    @NotNull
    private Screen.ScreenType screenType;
    @NotNull
    private Integer totalSeats;
}
