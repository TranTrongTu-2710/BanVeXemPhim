package com.example.demo.services.serviceIplm;

import com.example.demo.model.FoodItem;
import com.example.demo.repository.FoodItemRepository;
import com.example.demo.request.foodItem.CreateFoodItemRequest;
import com.example.demo.request.foodItem.UpdateFoodItemRequest;
import com.example.demo.services.FoodItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodItemServiceImpl implements FoodItemService {

    private final FoodItemRepository foodItemRepository;

    @Override
    public FoodItem createFoodItem(CreateFoodItemRequest request) {
        FoodItem foodItem = FoodItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .build();
        return foodItemRepository.save(foodItem);
    }

    @Override
    public FoodItem getFoodItemById(Integer id) {
        return foodItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food item not found"));
    }

    @Override
    public List<FoodItem> getAllFoodItems() {
        return foodItemRepository.findAll();
    }

    @Override
    public List<FoodItem> getAvailableFoodItems() {
        return foodItemRepository.findByIsAvailable(true);
    }

    @Override
    public FoodItem updateFoodItem(Integer id, UpdateFoodItemRequest request) {
        FoodItem foodItem = getFoodItemById(id);

        if (request.getName() != null) foodItem.setName(request.getName());
        if (request.getDescription() != null) foodItem.setDescription(request.getDescription());
        if (request.getCategory() != null) foodItem.setCategory(request.getCategory());
        if (request.getPrice() != null) foodItem.setPrice(request.getPrice());
        
        // Kiểm tra nếu URL ảnh mới khác URL ảnh hiện tại thì mới cập nhật
        if (request.getImageUrl() != null && !request.getImageUrl().equals(foodItem.getImageUrl())) {
            foodItem.setImageUrl(request.getImageUrl());
        }

        if (request.getIsAvailable() != null) foodItem.setIsAvailable(request.getIsAvailable());

        return foodItemRepository.save(foodItem);
    }

    @Override
    public void deleteFoodItem(Integer id) {
        FoodItem foodItem = getFoodItemById(id);
        foodItem.setIsAvailable(false); // Soft delete
        foodItemRepository.save(foodItem);
    }
}
