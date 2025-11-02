package com.ticketing.pricing.model;

import com.ticketing.common.model.TicketType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DiscountRule {
    private String name;
    private TicketType applicableTicketType;
    private BigDecimal discountPercentage;
    private Integer minQuantity;
    private DiscountCondition condition;
    private boolean enabled;
}
