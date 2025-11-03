package com.ticketing.service;

import com.ticketing.common.util.TicketClassificationUtil;
import com.ticketing.dto.request.CustomerRequest;
import com.ticketing.dto.request.TicketTransactionRequest;
import com.ticketing.dto.response.TicketSegment;
import com.ticketing.dto.response.TicketTransactionResponse;
import com.ticketing.exception.InvalidTransactionException;
import com.ticketing.common.model.TicketType;
import com.ticketing.pricing.service.PricingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class TicketProcessingService {

    private final PricingService pricingService;

    public TicketProcessingService(
            PricingService pricingService) {
        this.pricingService = pricingService;
    }

    public TicketTransactionResponse processTransaction(TicketTransactionRequest request) {
        validateRequest(request);

        var ticketTransactionResponse = TicketTransactionResponse.builder()
                .transactionId(request.getTransactionId())
                .tickets(new ArrayList<>())
                .build();

        Map<TicketType, Integer> ticketCounts = new HashMap<>();

        for (CustomerRequest customer : request.getCustomers()) {
            if (customer.getAge() < 0) {
                throw new InvalidTransactionException("Invalid age for customer: " + customer.getName());
            }

            TicketType ticketType = TicketClassificationUtil.classifyByAge(customer.getAge());
            ticketCounts.merge(ticketType, 1, Integer::sum);
        }

        BigDecimal totalCost = BigDecimal.ZERO;
        List<TicketSegment> ticketSegments = new ArrayList<>();

        for (Map.Entry<TicketType, Integer> entry : ticketCounts.entrySet()) {
            var ticketTypePrice = pricingService.calculateTicketPrice(entry.getKey(), ticketCounts);
            var ticketSegment = TicketSegment.builder()
                    .ticketType(entry.getKey())
                    .quantity(entry.getValue())
                    .totalCost(
                            ticketTypePrice.getFinalPrice()
                                    .multiply(
                                            BigDecimal.valueOf(entry.getValue())
                                    ).setScale(2, RoundingMode.HALF_UP)
                    )
                    .build();
            ticketSegments.add(ticketSegment);
            totalCost = totalCost.add(ticketSegment.getTotalCost());
        }

        // Sort tickets by ticket type alphabetically
        ticketSegments.sort(Comparator.comparing(segment -> segment.getTicketType().name()));
        ticketTransactionResponse.setTickets(ticketSegments);
        ticketTransactionResponse.setTotalCost(totalCost);

        return ticketTransactionResponse;
    }

    private void validateRequest(TicketTransactionRequest request) {
        if (request.getCustomers() == null || request.getCustomers().isEmpty()) {
            throw new InvalidTransactionException("Transaction must include at least one customer");
        }
    }
}
