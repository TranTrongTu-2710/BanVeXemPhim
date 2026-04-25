package com.example.demo.services;

import com.example.demo.model.Screen;
import com.example.demo.request.screen.CreateScreenRequest;
import com.example.demo.request.screen.UpdateScreenRequest;

import java.util.List;

public interface ScreenService {
    Screen createScreen(CreateScreenRequest request);
    Screen getScreenById(Integer id);
    List<Screen> getAllScreens();
    List<Screen> getScreensByCinema(Integer cinemaId);
    Screen updateScreen(Integer id, UpdateScreenRequest request);
    void deleteScreen(Integer id);
}
