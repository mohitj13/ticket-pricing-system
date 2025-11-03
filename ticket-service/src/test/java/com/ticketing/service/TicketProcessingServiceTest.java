package com.ticketing.service;

import com.ticketing.common.model.TicketType;
import com.ticketing.dto.request.CustomerRequest;
import com.ticketing.dto.request.TicketTransactionRequest;
import com.ticketing.dto.response.TicketTransactionResponse;
import com.ticketing.exception.InvalidTransactionException;
import com.ticketing.pricing.model.PriceCalculationResult;
import com.ticketing.pricing.service.PricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketProcessingServiceTest {

    @Mock
    private PricingService pricingService;

    private TicketProcessingService ticketProcessingService;

    @BeforeEach
    void setUp() {
        ticketProcessingService = new TicketProcessingService(
                pricingService
        );
    }

    @Test
    void processTransaction_withValidRequest_shouldSaveAndReturnResponse() {
        // Given
        CustomerRequest customer = CustomerRequest.builder()
                .name("John Doe")
                .age(30)
                .build();

        TicketTransactionRequest request = TicketTransactionRequest.builder()
                .customers(List.of(customer))
                .transactionId(1234L)
                .build();

        when(pricingService.calculateTicketPrice(any(), anyMap()))
                .thenReturn(PriceCalculationResult.builder()
                        .finalPrice(new BigDecimal("25.00"))
                        .build());

        // When
        TicketTransactionResponse result = ticketProcessingService.processTransaction(request);

        // Then
        assertNotNull(result);
        verify(pricingService).calculateTicketPrice(any(), anyMap());

        assertNotNull(result);
        assertEquals(1234L, request.getTransactionId());
        assertEquals(new BigDecimal("25.00"), result.getTickets().getFirst().getTotalCost());
        assertEquals(new BigDecimal("25.00"), result.getTotalCost());
    }

    @Test
    void processTransaction_withEmptyCustomers_shouldThrowException() {
        // Given
        TicketTransactionRequest request = TicketTransactionRequest.builder()
                .customers(new ArrayList<>())
                .build();

        // When & Then
        assertThrows(InvalidTransactionException.class,
                () -> ticketProcessingService.processTransaction(request));
    }

    @Test
    void processTransaction_withNullCustomers_shouldThrowException() {
        // Given
        TicketTransactionRequest request = TicketTransactionRequest.builder()
                .customers(null)
                .build();

        // When & Then
        assertThrows(InvalidTransactionException.class,
                () -> ticketProcessingService.processTransaction(request));
    }

    @Test
    void processTransaction_withInvalidBirthDate_shouldThrowException() {
        // Given
        CustomerRequest customer = CustomerRequest.builder()
                .name("Invalid Customer")
                .age(-1)
                .build();

        TicketTransactionRequest request = TicketTransactionRequest.builder()
                .customers(List.of(customer))
                .build();

        // When & Then
        assertThrows(InvalidTransactionException.class,
                () -> ticketProcessingService.processTransaction(request));
    }

    @Test
    void processTransaction_withMultipleCustomers_shouldProcessAll() {
        // Given
        CustomerRequest adult = CustomerRequest.builder()
                .name("Adult Customer")
                .age(21)
                .build();

        CustomerRequest child = CustomerRequest.builder()
                .name("Child Customer")
                .age(8)
                .build();

        CustomerRequest teen = CustomerRequest.builder()
                .name("Teen Customer")
                .age(12)
                .build();


        TicketTransactionRequest request = TicketTransactionRequest.builder()
                .customers(List.of(adult, child, teen))
                .build();

        when(pricingService.calculateTicketPrice(eq(TicketType.ADULT), anyMap()))
                .thenReturn(PriceCalculationResult.builder()
                        .finalPrice(new BigDecimal("25.00"))
                        .build());
        when(pricingService.calculateTicketPrice(eq(TicketType.TEEN), anyMap()))
                .thenReturn(PriceCalculationResult.builder()
                        .finalPrice(new BigDecimal("10.00"))
                        .build());
        when(pricingService.calculateTicketPrice(eq(TicketType.CHILD), anyMap()))
                .thenReturn(PriceCalculationResult.builder()
                        .finalPrice(new BigDecimal("5.00"))
                        .build());

        // When
        TicketTransactionResponse result = ticketProcessingService.processTransaction(request);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("40.00"), result.getTotalCost());
        assertEquals(new BigDecimal("25.00"), result.getTickets().stream()
                .filter(t -> t.getTicketType() == TicketType.ADULT)
                .findFirst()
                .get()
                .getTotalCost());
        assertEquals(new BigDecimal("10.00"), result.getTickets().stream()
                .filter(t -> t.getTicketType() == TicketType.TEEN)
                .findFirst()
                .get()
                .getTotalCost());
        assertEquals(new BigDecimal("5.00"), result.getTickets().stream()
                .filter(t -> t.getTicketType() == TicketType.CHILD)
                .findFirst()
                .get()
                .getTotalCost());
        verify(pricingService, times(3)).calculateTicketPrice(any(), anyMap());

    }

    @Test
    void processTransaction_shouldReturnTicketsInAlphabeticalOrder() {
        // Given
        List<CustomerRequest> customers = List.of(
                CustomerRequest.builder().name("Child").age(10).build(),
                CustomerRequest.builder().name("Senior").age(70).build(),
                CustomerRequest.builder().name("Adult").age(30).build()
        );

        TicketTransactionRequest request = TicketTransactionRequest.builder()
                .customers(customers)
                .transactionId(1234L)
                .build();

        when(pricingService.calculateTicketPrice(any(), anyMap()))
                .thenReturn(PriceCalculationResult.builder()
                        .finalPrice(new BigDecimal("25.00"))
                        .build());

        // When
        TicketTransactionResponse result = ticketProcessingService.processTransaction(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getTickets());
        assertTrue(result.getTickets().size() >= 2, "Should have at least 2 different ticket types");

        // Verify tickets are in alphabetical order
        for (int i = 0; i < result.getTickets().size() - 1; i++) {
            String currentType = result.getTickets().get(i).getTicketType().name();
            String nextType = result.getTickets().get(i + 1).getTicketType().name();
            assertTrue(currentType.compareTo(nextType) <= 0,
                    "Tickets should be sorted alphabetically by type. Found " + currentType + " before " + nextType);
        }
    }
}
