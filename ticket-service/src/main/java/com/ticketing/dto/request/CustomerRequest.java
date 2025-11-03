package com.ticketing.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerRequest {
    @NotBlank(message = "Name cannot be blank")
    private String name;
    @NotNull(message = "Age cannot be empty")
    @Min(value = 0, message = "Age cannot be negative")
    private Integer age;
}
