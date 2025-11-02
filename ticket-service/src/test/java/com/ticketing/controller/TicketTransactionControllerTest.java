package com.ticketing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.common.model.TicketType;
import com.ticketing.dto.request.CustomerRequest;
import com.ticketing.dto.request.TicketTransactionRequest;
import com.ticketing.dto.response.TicketSegment;
import com.ticketing.dto.response.TicketTransactionResponse;
import com.ticketing.exception.handler.ApplicationExceptionHandler;
import com.ticketing.service.TicketProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TicketTransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TicketProcessingService ticketProcessingService;

    @InjectMocks
    private TicketTransactionController ticketTransactionController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders.standaloneSetup(ticketTransactionController)
                .setControllerAdvice(new ApplicationExceptionHandler())
                .build();
    }

    @Test
    void processTicket_withValidRequest_shouldReturnCreated() throws Exception {
        // Given
        CustomerRequest customer = CustomerRequest.builder()
                .name("John Doe")
                .age(25)
                .build();

        TicketTransactionRequest request = TicketTransactionRequest.builder()
                .customers(List.of(customer))
                .transactionId(1234L)
                .build();

        TicketTransactionResponse response = TicketTransactionResponse.builder()
                .transactionId(1234L)
                .totalCost(new BigDecimal("25.00"))
                .tickets(new ArrayList<>())
                .build();

        when(ticketProcessingService.processTransaction(any(TicketTransactionRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/tickets/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(1234L))
                .andExpect(jsonPath("$.totalCost").value(25.00));

        verify(ticketProcessingService).processTransaction(any(TicketTransactionRequest.class));
    }

    @Test
    void processTicket_withEmptyCustomers_shouldReturnBadRequest() throws Exception {
        // Given
        TicketTransactionRequest request = TicketTransactionRequest.builder()
                .customers(new ArrayList<>())
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/tickets/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(ticketProcessingService, never()).processTransaction(any());
    }

    @Test
    void processTicket_withNullName_shouldReturnBadRequest() throws Exception {
        // Given
        CustomerRequest customer = CustomerRequest.builder()
                .name(null)
                .age(25)
                .build();

        TicketTransactionRequest request = TicketTransactionRequest.builder()
                .customers(List.of(customer))
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/tickets/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void processTicket_withFutureBirthDate_shouldReturnBadRequest() throws Exception {
        // Given
        CustomerRequest customer = CustomerRequest.builder()
                .name("Future Person")
                .age(-1)
                .build();

        TicketTransactionRequest request = TicketTransactionRequest.builder()
                .customers(List.of(customer))
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/tickets/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processTicket_withInvalidRequest_shouldReturnBadRequest() throws Exception {
        // Given
        TicketTransactionRequest request = TicketTransactionRequest.builder()
                .customers(List.of(CustomerRequest.builder().build()))
                .build();
        // When & Then
        mockMvc.perform(post("/api/v1/tickets/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processTicket_withMultipleCustomers_shouldReturnCreated() throws Exception {
        // Given
        CustomerRequest adult = CustomerRequest.builder()
                .name("Adult Customer")
                .age(30)
                .build();

        CustomerRequest child = CustomerRequest.builder()
                .name("Child Customer")
                .age(8)
                .build();

        TicketTransactionRequest request = TicketTransactionRequest.builder()
                .customers(List.of(adult, child))
                .transactionId(12345L)
                .build();

        TicketTransactionResponse response = TicketTransactionResponse.builder()
                .transactionId(12345L)
                .totalCost(new BigDecimal("35.00"))
                .tickets(List.of(TicketSegment.builder().ticketType(TicketType.ADULT).totalCost(new BigDecimal("25.00")).quantity(1).build(),
                        TicketSegment.builder().ticketType(TicketType.CHILD).totalCost(new BigDecimal("10.00")).quantity(1).build()))
                .build();

        when(ticketProcessingService.processTransaction(any(TicketTransactionRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/tickets/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCost").value(35.00))
                .andExpect(jsonPath("$.tickets.length()").value(2))
                .andExpect(jsonPath("$.tickets[?(@.ticketType=='ADULT')].quantity").value(1))
                .andExpect(jsonPath("$.tickets[?(@.ticketType=='CHILD')].quantity").value(1))
                .andExpect(jsonPath("$.tickets[?(@.ticketType=='ADULT')].totalCost").value(25.00))
                .andExpect(jsonPath("$.tickets[?(@.ticketType=='CHILD')].totalCost").value(10.00));

    }
}
