package com.ticketing.pricing.service;

import com.ticketing.common.model.TicketType;
import com.ticketing.pricing.model.PriceCalculationResult;

import java.math.BigDecimal;
import java.util.Map;

public interface PricingService {
    PriceCalculationResult calculateTicketPrice(TicketType ticketType, Map<TicketType, Integer> counts);

    BigDecimal getTicketBasePrice(TicketType ticketType);
}
