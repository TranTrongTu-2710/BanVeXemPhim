package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    Long countNewUsersByDateRange(@Param("startDate") LocalDateTime startDateTime, @Param("endDate") LocalDateTime endDateTime);
}
