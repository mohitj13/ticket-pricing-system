package com.ticketing.common.util;

import com.ticketing.common.model.TicketType;

public class TicketClassificationUtil {

    public static TicketType classifyByAge(int age) {
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }

        if (age <= 10) {
            return TicketType.CHILD;
        } else if (age <= 17) {
            return TicketType.TEEN;
        } else if (age <= 64) {
            return TicketType.ADULT;
        } else {
            return TicketType.SENIOR;
        }
    }

}
