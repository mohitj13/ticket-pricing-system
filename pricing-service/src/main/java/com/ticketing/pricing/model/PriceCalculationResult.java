package com.ticketing.pricing.model;

import com.ticketing.common.model.TicketType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PriceCalculationResult {
    private TicketType ticketType;
    private BigDecimal totalCost;
    private BigDecimal basePrice;
    private BigDecimal finalPrice;
    private BigDecimal discountAmount;
    private List<String> appliedDiscounts;
}
