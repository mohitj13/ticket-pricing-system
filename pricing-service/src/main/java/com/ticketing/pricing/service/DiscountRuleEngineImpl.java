package com.ticketing.pricing.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ticketing.common.model.TicketType;
import com.ticketing.pricing.config.PricingConfiguration;
import com.ticketing.pricing.model.DiscountCondition;
import com.ticketing.pricing.model.DiscountRule;
import com.ticketing.pricing.model.PriceCalculationResult;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DiscountRuleEngineImpl implements DiscountRuleEngine {

    private final PricingConfiguration pricingConfiguration;
    private static final RoundingMode PRICE_ROUNDING_MODE = RoundingMode.HALF_UP;

    public DiscountRuleEngineImpl(PricingConfiguration pricingConfiguration) {
        this.pricingConfiguration = pricingConfiguration;
    }

    @Override
    public PriceCalculationResult applyDiscounts(TicketType ticketType, BigDecimal basePrice, Map<TicketType, Integer> counts) {
        List<String> appliedDiscounts = new ArrayList<>();
        BigDecimal finalPrice = basePrice;
        BigDecimal totalDiscount = BigDecimal.ZERO.setScale(2, PRICE_ROUNDING_MODE);

        for (DiscountRule rule : pricingConfiguration.getDiscounts()) {
            if (!rule.isEnabled()) {
                continue;
            }

            if (isDiscountApplicable(rule, ticketType, counts)) {
                BigDecimal discountAmount = calculateDiscountAmount(finalPrice, rule.getDiscountPercentage());
                finalPrice = finalPrice.subtract(discountAmount);
                totalDiscount = totalDiscount.add(discountAmount);
                appliedDiscounts.add(rule.getName());
            }
        }

        return PriceCalculationResult.builder()
                .ticketType(ticketType)
                .basePrice(basePrice)
                .finalPrice(finalPrice)
                .discountAmount(totalDiscount)
                .appliedDiscounts(appliedDiscounts)
                .build();
    }

    private boolean isDiscountApplicable(DiscountRule rule, TicketType ticketType, Map<TicketType, Integer> counts) {
        if (rule.getCondition() == DiscountCondition.GROUP_DISCOUNT) {
            // For GROUP_DISCOUNT, count all tickets regardless of type
            int totalQuantity = counts.values().stream().mapToInt(Integer::intValue).sum();
            return totalQuantity >= rule.getMinQuantity(); // Group discount applies for more than 5 tickets
        }

        // Check if the discount applies to this ticket type for non-GROUP_DISCOUNT rules
        if (rule.getApplicableTicketType() != null && !rule.getApplicableTicketType().equals(ticketType)) {
            return false;
        }

        if (rule.getCondition() == DiscountCondition.MIN_QUANTITY) {
            // For MIN_QUANTITY, only count tickets of the applicable type
            int relevantQuantity = counts.getOrDefault(rule.getApplicableTicketType(), 0);
            return relevantQuantity >= rule.getMinQuantity();
        } else if (rule.getCondition() == DiscountCondition.TICKET_TYPE) {
            // For TICKET_TYPE, just check if the ticket type matches
            return rule.getApplicableTicketType() == ticketType;
        }

        return false;
    }

    private BigDecimal calculateDiscountAmount(BigDecimal price, BigDecimal discountPercentage) {
        return price.multiply(discountPercentage)
                .divide(new BigDecimal("100"), 2, PRICE_ROUNDING_MODE);
    }
}
