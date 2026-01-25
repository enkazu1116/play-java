package com.playjava.usecase.dto;

import lombok.Data;
import com.playjava.enterprise.entity.TOrder;
import com.playjava.enterprise.entity.TOrderItem;
import java.util.List;

@Data
public class OrderDetailResponse {
    private TOrder order;
    private List<TOrderItem> items;
}
