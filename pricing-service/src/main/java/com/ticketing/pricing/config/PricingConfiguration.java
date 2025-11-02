package com.ticketing.pricing.config;

import com.ticketing.pricing.model.DiscountRule;
import com.ticketing.pricing.model.PricingRule;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "pricing")
@Data
public class PricingConfiguration {
    private List<PricingRule> rules = new ArrayList<>();
    private List<DiscountRule> discounts = new ArrayList<>();
}
