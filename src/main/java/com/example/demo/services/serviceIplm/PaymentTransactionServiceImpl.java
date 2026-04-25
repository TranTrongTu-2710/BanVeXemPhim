package com.example.demo.services.serviceIplm;

import com.example.demo.model.PaymentTransaction;
import com.example.demo.repository.PaymentTransactionRepository;
import com.example.demo.services.PaymentTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentTransactionServiceImpl implements PaymentTransactionService {

    private final PaymentTransactionRepository paymentTransactionRepository;

    @Override
    public List<PaymentTransaction> getTransactionsByBooking(Integer bookingId) {
        return paymentTransactionRepository.findByBookingId(bookingId);
    }

    @Override
    public List<PaymentTransaction> getAllTransactions() {
        return paymentTransactionRepository.findAll();
    }
}
