package com.ticketing.pricing;

import com.ticketing.pricing.config.PricingConfiguration;
import com.ticketing.pricing.model.PriceCalculationResult;
import com.ticketing.common.model.TicketType;
import com.ticketing.pricing.service.DiscountRuleEngineImpl;
import com.ticketing.pricing.service.PricingService;
import com.ticketing.pricing.service.PricingServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        PricingConfiguration.class,
        PricingServiceImpl.class,
        DiscountRuleEngineImpl.class
})
@EnableConfigurationProperties
@TestPropertySource(properties = {
        "pricing.rules[0].ticketType=ADULT",
        "pricing.rules[0].basePrice=25.00",
        "pricing.rules[1].ticketType=CHILD",
        "pricing.rules[1].basePrice=10.00",
        "pricing.rules[2].ticketType=SENIOR",
        "pricing.rules[2].basePrice=15.00",
        "pricing.discounts[0].name=Group Discount",
        "pricing.discounts[0].discountPercentage=10",
        "pricing.discounts[0].minQuantity=4",
        "pricing.discounts[0].condition=GROUP_DISCOUNT",
        "pricing.discounts[0].enabled=true",
        "pricing.discounts[1].name=Senior Discount",
        "pricing.discounts[1].applicableTicketType=SENIOR",
        "pricing.discounts[1].discountPercentage=5",
        "pricing.discounts[1].condition=TICKET_TYPE",
        "pricing.discounts[1].enabled=true"
})
class PricingCalculationTest {

    @Autowired
    private PricingService pricingService;

    @Autowired
    private PricingConfiguration pricingConfiguration;

    @Test
    void contextLoads() {
        assertNotNull(pricingService);
        assertNotNull(pricingConfiguration);
    }

    @Test
    void pricingConfiguration_shouldLoadRulesCorrectly() {
        // When
        var rules = pricingConfiguration.getRules();

        // Then
        assertNotNull(rules);
        assertTrue(rules.size() >= 3);

        assertTrue(rules.stream()
                .anyMatch(r -> TicketType.ADULT.equals(r.getTicketType())));
    }

    @Test
    void calculatePrice_endToEnd_singleAdultTicket() {
        // Given
        Map<TicketType, Integer> counts = Map.of(TicketType.ADULT, 1);

        // When
        PriceCalculationResult result = pricingService.calculateTicketPrice(TicketType.ADULT, counts);

        // Then
        assertEquals(TicketType.ADULT, result.getTicketType());
        assertEquals(new BigDecimal("25.00"), result.getBasePrice());
        assertEquals(new BigDecimal("25.00"), result.getFinalPrice());
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result.getDiscountAmount());
    }

    @Test
    void calculatePrice_endToEnd_groupDiscount() {
        // Given
        Map<TicketType, Integer> counts = Map.of(TicketType.ADULT, 4);

        // When
        PriceCalculationResult result = pricingService.calculateTicketPrice(TicketType.ADULT, counts);

        // Then
        assertEquals(new BigDecimal("25.00"), result.getBasePrice());
        assertEquals(new BigDecimal("22.50"), result.getFinalPrice());
        assertTrue(result.getAppliedDiscounts().contains("Group Discount"));
    }

    @Test
    void calculatePrice_endToEnd_seniorWithMultipleDiscounts() {
        // Given
        Map<TicketType, Integer> counts = Map.of(TicketType.SENIOR, 4);

        // When
        PriceCalculationResult result = pricingService.calculateTicketPrice(TicketType.SENIOR, counts);

        // Then
        assertEquals(new BigDecimal("15.00"), result.getBasePrice());
        // 15.00 - 10% = 13.50, then 13.50 - 5% = 12.82
        assertEquals(new BigDecimal("12.82"), result.getFinalPrice());
        assertEquals(2, result.getAppliedDiscounts().size());
    }
}
