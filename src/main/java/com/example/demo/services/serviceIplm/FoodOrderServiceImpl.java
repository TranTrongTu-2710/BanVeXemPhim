package com.example.demo.services.serviceIplm;

import com.example.demo.model.FoodItem;
import com.example.demo.model.FoodOrder;
import com.example.demo.model.FoodOrderItem;
import com.example.demo.model.User;
import com.example.demo.repository.FoodItemRepository;
import com.example.demo.repository.FoodOrderRepository;
import com.example.demo.request.food_order.CreateFoodOrderRequest;
import com.example.demo.response.FoodOrderResponseDTO;
import com.example.demo.services.FoodOrderService;
import com.example.demo.services.SecurityService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FoodOrderServiceImpl implements FoodOrderService {

    private final FoodOrderRepository foodOrderRepository;
    private final FoodItemRepository foodItemRepository;
    private final SecurityService securityService;

    @Override
    @Transactional
    public FoodOrderResponseDTO createFoodOrder(CreateFoodOrderRequest request) {
        User currentStaff = securityService.getCurrentUser();
        securityService.checkStaffCinemaAccess(request.getCinemaId());

        BigDecimal finalAmount = BigDecimal.ZERO;
        List<FoodOrderItem> orderItems = new ArrayList<>();

        if (request.getFoodItems() == null || request.getFoodItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Food items cannot be empty.");
        }

        for (CreateFoodOrderRequest.FoodOrderItemRequest itemRequest : request.getFoodItems()) {
            FoodItem foodItem = foodItemRepository.findById(itemRequest.getFoodItemId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food item with id " + itemRequest.getFoodItemId() + " not found."));

            if (!foodItem.getIsAvailable()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Food item " + foodItem.getName() + " is not available.");
            }

            BigDecimal itemTotal = foodItem.getPrice().multiply(new BigDecimal(itemRequest.getQuantity()));
            finalAmount = finalAmount.add(itemTotal);

            orderItems.add(FoodOrderItem.builder()
                    .foodItem(foodItem)
                    .quantity(itemRequest.getQuantity())
                    .priceAtPurchase(foodItem.getPrice())
                    .build());
        }

        FoodOrder foodOrder = FoodOrder.builder()
                .staff(currentStaff)
                .finalAmount(finalAmount)
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
                .status(FoodOrder.OrderStatus.COMPLETED)
                .build();

        for (FoodOrderItem item : orderItems) {
            item.setFoodOrder(foodOrder);
        }
        foodOrder.setOrderItems(orderItems);

        FoodOrder savedOrder = foodOrderRepository.save(foodOrder);
        return new FoodOrderResponseDTO(savedOrder);
    }

    @Override
    public List<FoodOrderResponseDTO> getAllFoodOrdersDTO() {
        return foodOrderRepository.findAll().stream()
                .map(FoodOrderResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public FoodOrderResponseDTO getFoodOrderDTOById(Integer id) {
        FoodOrder foodOrder = foodOrderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food order not found."));
        return new FoodOrderResponseDTO(foodOrder);
    }
}
