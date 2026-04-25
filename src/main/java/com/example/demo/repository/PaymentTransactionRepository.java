package com.example.demo.repository;

import com.example.demo.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Integer> {
    Optional<PaymentTransaction> findByTransactionId(String transactionId);
    List<PaymentTransaction> findByBookingId(Integer bookingId);
}
