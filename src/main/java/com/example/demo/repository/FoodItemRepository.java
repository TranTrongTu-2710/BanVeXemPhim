package com.example.demo.repository;

import com.example.demo.model.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Integer> {
    List<FoodItem> findByCategory(FoodItem.FoodCategory category);
    List<FoodItem> findByIsAvailable(boolean isAvailable);
}
