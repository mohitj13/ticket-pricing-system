package com.ticketing.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.dto.request.CustomerRequest;
import com.ticketing.dto.request.TicketTransactionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TicketTransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void processTicketsTransactions() throws Exception {

        CustomerRequest customer = CustomerRequest.builder()
                .name("Adult movie Customer")
                .age(35)
                .build();

        TicketTransactionRequest request = TicketTransactionRequest.builder()
                .customers(List.of(customer))
                .transactionId(21L)
                .build();

        mockMvc.perform(post("/api/v1/tickets/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.totalCost").value(25.00))
                .andReturn();
    }

    @Test
    void processTicketsTransactions_withMultipleCustomers_shouldApplyCorrectPricing() throws Exception {

        CustomerRequest adult = CustomerRequest.builder()
                .name("Adult Customer")
                .age(35)
                .build();

        CustomerRequest child = CustomerRequest.builder()
                .name("Child Customer")
                .age(5)
                .build();

        CustomerRequest senior = CustomerRequest.builder()
                .name("Senior Customer")
                .age(72)
                .build();

        TicketTransactionRequest request = TicketTransactionRequest.builder()
                .customers(List.of(adult, child, senior))
                .transactionId(21L)
                .build();

        mockMvc.perform(post("/api/v1/tickets/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(21))
                .andExpect(jsonPath("$.totalCost").value(47.50))
                .andExpect(jsonPath("$.tickets").isArray())
                .andExpect(jsonPath("$.tickets.length()").value(3))
                .andExpect(jsonPath("$.tickets[?(@.ticketType=='ADULT')].quantity").value(1))
                .andExpect(jsonPath("$.tickets[?(@.ticketType=='CHILD')].quantity").value(1))
                .andExpect(jsonPath("$.tickets[?(@.ticketType=='SENIOR')].quantity").value(1))
                .andExpect(jsonPath("$.tickets[?(@.ticketType=='ADULT')].totalCost").value(25.00))
                .andExpect(jsonPath("$.tickets[?(@.ticketType=='SENIOR')].totalCost").value(17.50))
                .andExpect(jsonPath("$.tickets[?(@.ticketType=='CHILD')].totalCost").value(5.00));
    }

    @Test
    void processTicketsTransactions_withGroupDiscount_shouldApplyDiscount() throws Exception {
        // Given - 4 adult tickets to trigger group discount
        CustomerRequest customer1 = createCustomer("Customer 1", 35);
        CustomerRequest customer2 = createCustomer("Customer 2", 34);
        CustomerRequest customer3 = createCustomer("Customer 3", 33);
        CustomerRequest customer4 = createCustomer("Customer 4", 32);

        TicketTransactionRequest request = TicketTransactionRequest.builder()
                .customers(List.of(customer1, customer2, customer3, customer4))
                .transactionId(21L)
                .build();

        mockMvc.perform(post("/api/v1/tickets/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(21))
                .andExpect(jsonPath("$.totalCost").value(85.00)) // 4 * 25.00 = 100.00 - 15% = 85.00
                .andExpect(jsonPath("$.tickets").isArray())
                .andExpect(jsonPath("$.tickets.length()").value(1))
                .andExpect(jsonPath("$.tickets[?(@.ticketType=='ADULT')].quantity").value(4))
                .andExpect(jsonPath("$.tickets[?(@.ticketType=='ADULT')].totalCost").value(85.00));
    }


    @Test
    void processTicketsTransactions_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Given
        CustomerRequest invalidCustomer = CustomerRequest.builder()
                .name("")
                .age(21)
                .build();

        TicketTransactionRequest request = TicketTransactionRequest.builder()
                .customers(List.of(invalidCustomer))
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/tickets/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void processTicketsTransactions_withChildGroupDiscount_shouldApplyDiscount() throws Exception {
        // Given
        CustomerRequest customer1 = createCustomer("Customer 1", 35);
        CustomerRequest customer2 = createCustomer("Customer 2", 10);
        CustomerRequest customer3 = createCustomer("Customer 3", 8);
        CustomerRequest customer4 = createCustomer("Customer 4", 7);
        CustomerRequest customer5 = createCustomer("Customer 5", 6);

        TicketTransactionRequest request = TicketTransactionRequest.builder()
                .customers(List.of(customer1, customer2, customer3, customer4, customer5))
                .transactionId(1234L)
                .build();

        mockMvc.perform(post("/api/v1/tickets/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(1234))
                // 5 - 25% = 3.75 - 15% = 3.19 * 4 = 12.76
                // 25.00 - 15% = 21.25
                // 12.75 + 21.25 = 34.01
                .andExpect(jsonPath("$.totalCost").value(34))
                .andExpect(jsonPath("$.tickets").isArray())
                .andExpect(jsonPath("$.tickets.length()").value(2))
                .andExpect(jsonPath("$.tickets[?(@.ticketType=='ADULT')].quantity").value(1))
                .andExpect(jsonPath("$.tickets[?(@.ticketType=='ADULT')].totalCost").value(21.25))
                .andExpect(jsonPath("$.tickets[?(@.ticketType=='CHILD')].quantity").value(4))
                .andExpect(jsonPath("$.tickets[?(@.ticketType=='CHILD')].totalCost").value(12.76));

    }

    private CustomerRequest createCustomer(String name, int age) {
        return CustomerRequest.builder()
                .name(name)
                .age(age)
                .build();
    }


}
