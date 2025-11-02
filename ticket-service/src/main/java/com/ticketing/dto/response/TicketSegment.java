package com.ticketing.dto.response;

import com.ticketing.common.model.TicketType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class TicketSegment {

    private TicketType ticketType;
    private long quantity;
    private BigDecimal totalCost;
}
