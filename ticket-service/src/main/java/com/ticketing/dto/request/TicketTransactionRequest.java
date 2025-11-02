package com.ticketing.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TicketTransactionRequest {
    @NotNull(message = "Transaction ID cannot be null")
    @Min(value = 1, message = "Transaction ID cannot be zero or negative")
    private Long transactionId;
    @Valid
    @NotEmpty(message = "Customer list cannot be empty")
    private List<CustomerRequest> customers;
}
