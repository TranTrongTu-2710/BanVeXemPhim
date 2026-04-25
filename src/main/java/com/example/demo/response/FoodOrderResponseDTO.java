package com.example.demo.response;

import com.example.demo.model.FoodOrder;
import com.example.demo.model.FoodOrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class FoodOrderResponseDTO {
    private Integer id;
    private BigDecimal finalAmount;
    private String paymentMethod;
    private FoodOrder.OrderStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private UserResponseDTO staff; // Thông tin nhân viên bán hàng
    private List<FoodOrderItemDTO> orderItems;

    public FoodOrderResponseDTO(FoodOrder foodOrder) {
        this.id = foodOrder.getId();
        this.finalAmount = foodOrder.getFinalAmount();
        this.paymentMethod = foodOrder.getPaymentMethod().toString();
        this.status = foodOrder.getStatus();
        this.notes = foodOrder.getNotes();
        this.createdAt = foodOrder.getCreatedAt();

        if (foodOrder.getStaff() != null) {
            this.staff = new UserResponseDTO(foodOrder.getStaff());
        }

        if (foodOrder.getOrderItems() != null) {
            this.orderItems = foodOrder.getOrderItems().stream()
                    .map(FoodOrderItemDTO::new)
                    .collect(Collectors.toList());
        }
    }

    @Data
    public static class FoodOrderItemDTO {
        private String foodItemName;
        private Integer quantity;
        private BigDecimal priceAtPurchase;

        public FoodOrderItemDTO(FoodOrderItem item) {
            if (item.getFoodItem() != null) {
                this.foodItemName = item.getFoodItem().getName();
            }
            this.quantity = item.getQuantity();
            this.priceAtPurchase = item.getPriceAtPurchase();
        }
    }
}
