package com.ticketing.pricing.service;

import com.ticketing.common.model.TicketType;
import com.ticketing.pricing.model.PriceCalculationResult;

import java.math.BigDecimal;
import java.util.Map;

public interface DiscountRuleEngine {
    PriceCalculationResult applyDiscounts(TicketType ticketType, BigDecimal basePrice, Map<TicketType, Integer> counts);
}
