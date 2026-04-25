package com.example.demo.services;

import com.example.demo.model.SeatPrice;
import com.example.demo.request.seatprice.CreateSeatPriceRequest;
import com.example.demo.request.seatprice.UpdateSeatPriceRequest;

import java.util.List;

public interface SeatPriceService {
    SeatPrice createSeatPrice(CreateSeatPriceRequest request);
    SeatPrice getSeatPriceById(Integer id);
    List<SeatPrice> getSeatPricesByShowtime(Integer showtimeId);
    SeatPrice updateSeatPrice(Integer id, UpdateSeatPriceRequest request);
    void deleteSeatPrice(Integer id);
}
