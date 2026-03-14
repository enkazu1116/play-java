package com.playjava.usecase.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

@Data
public class CreateOrderRequest {
    @NotNull(message = "顧客IDは必須です")
    private UUID customerId;
    
    @NotEmpty(message = "注文明細は1件以上必要です")
    private List<OrderItemRequest> items;
}
