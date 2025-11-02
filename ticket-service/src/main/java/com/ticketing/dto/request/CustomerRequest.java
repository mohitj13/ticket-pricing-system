package com.ticketing.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerRequest {
    @NotBlank(message = "Name cannot be blank")
    private String name;
    @Min(value = 0, message = "Age cannot be negative")
    private int age;
}
