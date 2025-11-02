package com.ticketing.pricing.service;

import com.ticketing.pricing.config.PricingConfiguration;
import com.ticketing.pricing.exception.PricingRuleException;
import com.ticketing.pricing.model.PriceCalculationResult;
import com.ticketing.pricing.model.PricingRule;

import java.math.BigDecimal;
import java.util.Map;

import com.ticketing.common.model.TicketType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PricingServiceImpl implements PricingService {

    private final PricingConfiguration pricingConfiguration;
    private final DiscountRuleEngine discountRuleEngine;

    public PricingServiceImpl(PricingConfiguration pricingConfiguration, DiscountRuleEngine discountRuleEngine) {
        this.pricingConfiguration = pricingConfiguration;
        this.discountRuleEngine = discountRuleEngine;
        log.info("PricingService initialized with {} pricing rules and {} discount rules.",
                pricingConfiguration.getRules().size(),
                pricingConfiguration.getDiscounts().size());
    }

    @Override
    public PriceCalculationResult calculateTicketPrice(TicketType ticketType, Map<TicketType, Integer> counts) {
        BigDecimal basePrice = getTicketBasePrice(ticketType);
        return discountRuleEngine.applyDiscounts(ticketType, basePrice, counts);
    }

    @Override
    public BigDecimal getTicketBasePrice(TicketType ticketType) {
        return pricingConfiguration.getRules().stream()
                .filter(rule -> rule.getTicketType().equals(ticketType))
                .findFirst()
                .map(PricingRule::getBasePrice)
                .orElseThrow(() -> new PricingRuleException("No pricing rule found for ticket type: " + ticketType));
    }
}
