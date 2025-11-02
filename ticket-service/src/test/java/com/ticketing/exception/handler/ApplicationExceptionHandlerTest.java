package com.ticketing.exception.handler;

import com.ticketing.exception.InvalidTransactionException;
import com.ticketing.exception.PricingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationExceptionHandlerTest {

    private ApplicationExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new ApplicationExceptionHandler();
    }

    @Test
    void handleInvalidTransaction_shouldReturnBadRequest() {
        // Given
        InvalidTransactionException exception =
                new InvalidTransactionException("Invalid transaction data");

        // When
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleInvalidTransaction(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid transaction data", response.getBody().get("message"));
        assertEquals(400, response.getBody().get("status"));
    }

    @Test
    void handlePricingException_shouldReturnInternalServerError() {
        // Given
        PricingException exception = new PricingException("Pricing calculation failed");

        // When
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handlePricingException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Pricing calculation failed", response.getBody().get("message"));
        assertEquals(500, response.getBody().get("status"));
    }

    @Test
    void handleValidationExceptions_shouldReturnBadRequestWithErrors() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "fieldName", "Field is required");

        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        // When
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleValidationExceptions(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation Failed", response.getBody().get("error"));

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
        assertNotNull(errors);
        assertEquals("Field is required", errors.get("fieldName"));
    }

    @Test
    void handleGenericException_shouldReturnInternalServerError() {
        // Given
        Exception exception = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleGenericException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }
}
