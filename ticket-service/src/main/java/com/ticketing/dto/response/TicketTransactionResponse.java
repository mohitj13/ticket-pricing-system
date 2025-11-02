package com.ticketing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketTransactionResponse {

    private Long transactionId;
    private BigDecimal totalCost;
    private List<TicketSegment> tickets;
}
