package com.ticketing.common.util;

import com.ticketing.common.model.TicketType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class TicketClassificationUtilTest {

    @ParameterizedTest
    @CsvSource({
            "0, CHILD",
            "5, CHILD",
            "10, CHILD"
    })
    void classifyByAge_childRange_shouldReturnChild(int age, TicketType expected) {
        // When
        TicketType result = TicketClassificationUtil.classifyByAge(age);

        // Then
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({
            "11, TEEN",
            "15, TEEN",
            "17, TEEN"
    })
    void classifyByAge_teenRange_shouldReturnTeen(int age, TicketType expected) {
        // When
        TicketType result = TicketClassificationUtil.classifyByAge(age);

        // Then
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({
            "18, ADULT",
            "30, ADULT",
            "64, ADULT"
    })
    void classifyByAge_adultRange_shouldReturnAdult(int age, TicketType expected) {
        // When
        TicketType result = TicketClassificationUtil.classifyByAge(age);

        // Then
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({
            "65, SENIOR",
            "70, SENIOR",
            "100, SENIOR"
    })
    void classifyByAge_seniorRange_shouldReturnSenior(int age, TicketType expected) {
        // When
        TicketType result = TicketClassificationUtil.classifyByAge(age);

        // Then
        assertEquals(expected, result);
    }

    @Test
    void classifyByAge_negativeAge_shouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> TicketClassificationUtil.classifyByAge(-1));
    }

    @Test
    void classifyByAge_boundaryChild10_shouldReturnChild() {
        // When
        TicketType result = TicketClassificationUtil.classifyByAge(10);

        // Then
        assertEquals(TicketType.CHILD, result);
    }

    @Test
    void classifyByAge_boundaryTeen11_shouldReturnTeen() {
        // When
        TicketType result = TicketClassificationUtil.classifyByAge(11);

        // Then
        assertEquals(TicketType.TEEN, result);
    }

    @Test
    void classifyByAge_boundaryAdult18_shouldReturnAdult() {
        // When
        TicketType result = TicketClassificationUtil.classifyByAge(18);

        // Then
        assertEquals(TicketType.ADULT, result);
    }

    @Test
    void classifyByAge_boundarySenior65_shouldReturnSenior() {
        // When
        TicketType result = TicketClassificationUtil.classifyByAge(65);

        // Then
        assertEquals(TicketType.SENIOR, result);
    }
}
