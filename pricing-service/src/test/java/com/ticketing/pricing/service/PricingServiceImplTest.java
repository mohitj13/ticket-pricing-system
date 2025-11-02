package com.ticketing.pricing.service;

import com.ticketing.pricing.config.PricingConfiguration;
import com.ticketing.pricing.exception.PricingRuleException;
import com.ticketing.pricing.model.PriceCalculationResult;
import com.ticketing.pricing.model.PricingRule;
import com.ticketing.common.model.TicketType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingServiceImplTest {

    @Mock
    private PricingConfiguration pricingConfiguration;

    @Mock
    private DiscountRuleEngine discountRuleEngine;

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingServiceImpl(pricingConfiguration, discountRuleEngine);
    }

    @Test
    void calculatePrice_withValidTicketType_shouldReturnTicketPriceResult() {
        // Given
        TicketType ticketType = TicketType.ADULT;
        Map<TicketType, Integer> counts = Map.of(TicketType.ADULT, 1);

        PricingRule rule = PricingRule.builder()
                .ticketType(ticketType)
                .basePrice(new BigDecimal("25.00"))
                .build();

        PriceCalculationResult discountResult = PriceCalculationResult.builder()
                .ticketType(ticketType)
                .basePrice(new BigDecimal("25.00"))
                .finalPrice(new BigDecimal("25.00"))
                .discountAmount(BigDecimal.ZERO)
                .appliedDiscounts(new ArrayList<>())
                .build();

        when(pricingConfiguration.getRules()).thenReturn(List.of(rule));
        when(discountRuleEngine.applyDiscounts(eq(ticketType), any(), eq(counts)))
                .thenReturn(discountResult);

        // When
        PriceCalculationResult result = pricingService.calculateTicketPrice(ticketType, counts);

        // Then
        assertNotNull(result);
        assertEquals(ticketType, result.getTicketType());
        assertEquals(new BigDecimal("25.00"), result.getBasePrice());
        assertEquals(new BigDecimal("25.00"), result.getFinalPrice());

        verify(discountRuleEngine).applyDiscounts(eq(ticketType), any(), eq(counts));
    }

    @Test
    void getTicketBasePrice_withValidTicketType_shouldReturnPrice() {
        // Given
        TicketType ticketType = TicketType.CHILD;
        BigDecimal expectedPrice = new BigDecimal("10.00");

        PricingRule rule = PricingRule.builder()
                .ticketType(ticketType)
                .basePrice(expectedPrice)
                .build();

        when(pricingConfiguration.getRules()).thenReturn(List.of(rule));

        // When
        BigDecimal result = pricingService.getTicketBasePrice(ticketType);

        // Then
        assertEquals(expectedPrice, result);
    }

    @Test
    void getTicketBasePrice_withInvalidTicketType_shouldThrowException() {
        // Given
        when(pricingConfiguration.getRules()).thenReturn(new ArrayList<>());

        // When & Then
        assertThrows(
                PricingRuleException.class,
                () -> pricingService.getTicketBasePrice(null)
        );
    }

    @Test
    void calculateTicketPrice_withMultipleTicketTypes_shouldHandleCorrectly() {
        // Given
        TicketType ticketType = TicketType.SENIOR;
        Map<TicketType, Integer> counts = Map.of(
                TicketType.ADULT, 2,
                TicketType.SENIOR, 2
        );

        PricingRule rule = PricingRule.builder()
                .ticketType(ticketType)
                .basePrice(new BigDecimal("15.00"))
                .build();

        PriceCalculationResult discountResult = PriceCalculationResult.builder()
                .basePrice(new BigDecimal("15.00"))
                .finalPrice(new BigDecimal("12.75"))
                .discountAmount(new BigDecimal("2.25"))
                .appliedDiscounts(List.of("Senior Additional Discount"))
                .build();

        when(pricingConfiguration.getRules()).thenReturn(List.of(rule));
        when(discountRuleEngine.applyDiscounts(eq(ticketType), any(), eq(counts)))
                .thenReturn(discountResult);

        // When
        PriceCalculationResult result = pricingService.calculateTicketPrice(ticketType, counts);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("15.00"), result.getBasePrice());
        assertEquals(new BigDecimal("12.75"), result.getFinalPrice());
        assertEquals(1, result.getAppliedDiscounts().size());
    }
}
