package com.ticketing.pricing.model;

import com.ticketing.common.model.TicketType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PricingRule {
    private TicketType ticketType;
    private BigDecimal basePrice;
}
