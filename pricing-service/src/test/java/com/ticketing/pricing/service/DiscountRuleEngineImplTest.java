package com.ticketing.pricing.service;

import com.ticketing.pricing.config.PricingConfiguration;
import com.ticketing.pricing.model.DiscountCondition;
import com.ticketing.pricing.model.DiscountRule;
import com.ticketing.pricing.model.PriceCalculationResult;
import com.ticketing.common.model.TicketType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscountRuleEngineImplTest {

    @Mock
    private PricingConfiguration pricingConfiguration;

    private DiscountRuleEngine discountRuleEngine;

    @BeforeEach
    void setUp() {
        discountRuleEngine = new DiscountRuleEngineImpl(pricingConfiguration);
    }

    @Test
    void applyDiscounts_withNoDiscounts_shouldReturnBasePrice() {
        // Given
        TicketType ticketType = TicketType.ADULT;
        BigDecimal basePrice = new BigDecimal("25.00");
        Map<TicketType, Integer> counts = Map.of(TicketType.ADULT, 1);

        when(pricingConfiguration.getDiscounts()).thenReturn(new ArrayList<>());

        // When
        PriceCalculationResult result = discountRuleEngine.applyDiscounts(ticketType, basePrice, counts);

        // Then
        assertEquals(new BigDecimal("25.00"), result.getFinalPrice());
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result.getDiscountAmount());
        assertTrue(result.getAppliedDiscounts().isEmpty());
    }

    @Test
    void applyDiscounts_withChildGroupDiscount_shouldApplyCorrectly() {
        // Given
        TicketType ticketType = TicketType.CHILD;
        BigDecimal basePrice = new BigDecimal("25.00");
        Map<TicketType, Integer> counts = Map.of(TicketType.CHILD, 4);

        DiscountRule groupDiscount = DiscountRule.builder()
                .name("Child Group Discount")
                .applicableTicketType(TicketType.CHILD)
                .condition(DiscountCondition.MIN_QUANTITY)
                .discountPercentage(new BigDecimal("10"))
                .minQuantity(4)
                .enabled(true)
                .build();

        when(pricingConfiguration.getDiscounts()).thenReturn(List.of(groupDiscount));

        // When
        PriceCalculationResult result = discountRuleEngine.applyDiscounts(ticketType, basePrice, counts);

        // Then
        assertEquals(new BigDecimal("22.50"), result.getFinalPrice());
        assertEquals(new BigDecimal("2.50"), result.getDiscountAmount());
        assertEquals(1, result.getAppliedDiscounts().size());
        assertTrue(result.getAppliedDiscounts().contains("Child Group Discount"));
    }

    @Test
    void applyDiscounts_withSeniorDiscount_shouldApplyOnlyToSeniors() {
        // Given
        TicketType ticketType = TicketType.SENIOR;
        BigDecimal basePrice = new BigDecimal("15.00");
        Map<TicketType, Integer> counts = Map.of(TicketType.SENIOR, 1);

        DiscountRule seniorDiscount = DiscountRule.builder()
                .name("Senior Additional Discount")
                .applicableTicketType(ticketType)
                .discountPercentage(new BigDecimal("5"))
                .condition(DiscountCondition.TICKET_TYPE)
                .minQuantity(null)
                .enabled(true)
                .build();

        when(pricingConfiguration.getDiscounts()).thenReturn(List.of(seniorDiscount));

        // When
        PriceCalculationResult result = discountRuleEngine.applyDiscounts(ticketType, basePrice, counts);

        // Then
        assertEquals(new BigDecimal("14.25"), result.getFinalPrice());
        assertEquals(new BigDecimal("0.75"), result.getDiscountAmount());
    }

    @Test
    void applyDiscounts_withMultipleDiscounts_shouldApplyAllApplicable() {
        // Given
        TicketType ticketType = TicketType.SENIOR;
        BigDecimal basePrice = new BigDecimal("15.00");
        Map<TicketType, Integer> counts = Map.of(TicketType.SENIOR, 4, TicketType.ADULT, 1);

        DiscountRule groupDiscount = DiscountRule.builder()
                .name("Group Discount")
                .applicableTicketType(null)
                .discountPercentage(new BigDecimal("10"))
                .condition(DiscountCondition.GROUP_DISCOUNT)
                .minQuantity(4)
                .enabled(true)
                .build();

        DiscountRule seniorDiscount = DiscountRule.builder()
                .name("Senior Additional Discount")
                .applicableTicketType(ticketType)
                .discountPercentage(new BigDecimal("5"))
                .condition(DiscountCondition.TICKET_TYPE)
                .minQuantity(null)
                .enabled(true)
                .build();

        when(pricingConfiguration.getDiscounts())
                .thenReturn(List.of(groupDiscount, seniorDiscount));

        // When
        PriceCalculationResult result = discountRuleEngine.applyDiscounts(ticketType, basePrice, counts);

        // Then
        assertEquals(new BigDecimal("12.82"), result.getFinalPrice());
        assertEquals(2, result.getAppliedDiscounts().size());
    }

    @Test
    void applyDiscounts_withDisabledRule_shouldNotApply() {
        // Given
        TicketType ticketType = TicketType.ADULT;
        BigDecimal basePrice = new BigDecimal("25.00");
        Map<TicketType, Integer> counts = Map.of(TicketType.ADULT, 5);

        DiscountRule disabledDiscount = DiscountRule.builder()
                .name("Disabled Discount")
                .applicableTicketType(null)
                .discountPercentage(new BigDecimal("50"))
                .condition(DiscountCondition.MIN_QUANTITY)
                .minQuantity(1)
                .enabled(false)
                .build();

        when(pricingConfiguration.getDiscounts()).thenReturn(List.of(disabledDiscount));

        // When
        PriceCalculationResult result = discountRuleEngine.applyDiscounts(ticketType, basePrice, counts);

        // Then
        assertEquals(new BigDecimal("25.00"), result.getFinalPrice());
        assertTrue(result.getAppliedDiscounts().isEmpty());
    }

    @Test
    void applyDiscounts_withInsufficientQuantity_shouldNotApplyGroupDiscount() {
        // Given
        TicketType ticketType = TicketType.CHILD;
        BigDecimal basePrice = new BigDecimal("25.00");
        Map<TicketType, Integer> counts = Map.of(TicketType.CHILD, 3);

        DiscountRule groupDiscount = DiscountRule.builder()
                .name("Child Group Discount")
                .applicableTicketType(TicketType.CHILD)
                .discountPercentage(new BigDecimal("10"))
                .condition(DiscountCondition.MIN_QUANTITY)
                .minQuantity(4)
                .enabled(true)
                .build();

        when(pricingConfiguration.getDiscounts()).thenReturn(List.of(groupDiscount));

        // When
        PriceCalculationResult result = discountRuleEngine.applyDiscounts(ticketType, basePrice, counts);

        // Then
        assertEquals(new BigDecimal("25.00"), result.getFinalPrice());
        assertTrue(result.getAppliedDiscounts().isEmpty());
    }

    @Test
    void applyDiscounts_withWrongTicketType_shouldNotApply() {
        // Given
        TicketType ticketType = TicketType.ADULT;
        BigDecimal basePrice = new BigDecimal("25.00");
        Map<TicketType, Integer> counts = Map.of(TicketType.ADULT, 2);

        DiscountRule seniorDiscount = DiscountRule.builder()
                .name("Senior Discount")
                .applicableTicketType(TicketType.SENIOR)
                .discountPercentage(new BigDecimal("5"))
                .condition(DiscountCondition.TICKET_TYPE)
                .minQuantity(null)
                .enabled(true)
                .build();

        when(pricingConfiguration.getDiscounts()).thenReturn(List.of(seniorDiscount));

        // When
        PriceCalculationResult result = discountRuleEngine.applyDiscounts(ticketType, basePrice, counts);

        // Then
        assertEquals(new BigDecimal("25.00"), result.getFinalPrice());
        assertTrue(result.getAppliedDiscounts().isEmpty());
    }
}
