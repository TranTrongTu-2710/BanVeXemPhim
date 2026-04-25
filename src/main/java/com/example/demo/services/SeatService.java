package com.example.demo.services;

import com.example.demo.model.Seat;
import com.example.demo.request.seat.CreateSeatRequest;
import com.example.demo.request.seat.UpdateSeatRequest;

import java.util.List;

public interface SeatService {
    Seat createSeat(CreateSeatRequest request);
    Seat getSeatById(Integer id);
    List<Seat> getSeatsByScreen(Integer screenId);
    Seat updateSeat(Integer id, UpdateSeatRequest request);
    void deleteSeat(Integer id);
}
