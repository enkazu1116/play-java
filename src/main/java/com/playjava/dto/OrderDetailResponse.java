package com.playjava.dto;

import lombok.Data;
import com.playjava.entity.TOrder;
import com.playjava.entity.TOrderItem;
import java.util.List;

@Data
public class OrderDetailResponse {
    private TOrder order;
    private List<TOrderItem> items;
}
