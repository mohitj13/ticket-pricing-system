package com.ticketing.controller;

import com.ticketing.dto.request.TicketTransactionRequest;
import com.ticketing.dto.response.TicketTransactionResponse;
import com.ticketing.service.TicketProcessingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketTransactionController {

    private final TicketProcessingService ticketProcessingService;

    @PostMapping(
            path = "/transactions",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TicketTransactionResponse> processTicketTransaction(
            @Valid @RequestBody TicketTransactionRequest request) {
        log.info("Received ticket transaction Id : {}, request with {} customers",
                request.getTransactionId(), request.getCustomers().size());

        TicketTransactionResponse response = ticketProcessingService.processTransaction(request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
