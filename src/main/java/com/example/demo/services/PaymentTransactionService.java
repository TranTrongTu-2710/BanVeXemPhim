package com.example.demo.services;

import com.example.demo.model.PaymentTransaction;

import java.util.List;

public interface PaymentTransactionService {
    List<PaymentTransaction> getTransactionsByBooking(Integer bookingId);
    List<PaymentTransaction> getAllTransactions();
    // A method to create transaction would be here, called by a payment gateway webhook
    // PaymentTransaction createTransaction(...);
}
