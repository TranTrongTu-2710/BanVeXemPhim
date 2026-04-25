package com.example.demo.controller;

import com.example.demo.model.PaymentTransaction;
import com.example.demo.services.PaymentTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/payment-transactions")
@RequiredArgsConstructor
public class PaymentTransactionController {

    private final PaymentTransactionService paymentTransactionService;

    @GetMapping
    public ResponseEntity<List<PaymentTransaction>> getAllTransactions() {
        return ResponseEntity.ok(paymentTransactionService.getAllTransactions());
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PaymentTransaction>> getTransactionsByBooking(@PathVariable Integer bookingId) {
        return ResponseEntity.ok(paymentTransactionService.getTransactionsByBooking(bookingId));
    }
}
