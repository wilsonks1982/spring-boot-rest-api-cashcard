package com.wilsonks1982.cashcard2.data_transfer;

import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record CashCard(
        @Id Long id,
        @NotBlank(message = "Card number must not be blank")
        @Pattern(regexp = "\\d{10}", message = "Card number must be exactly 10 digits")
        String cardNumber,
        @NotNull(message = "Balance must not be null")
        @DecimalMin(value = "0.00", inclusive = true, message = "Balance must be non-negative")
        @DecimalMax(value = "1000000.00", inclusive = true, message = "Balance must be less than or equal to 1,000,000.00")
        BigDecimal balance,
        String owner) {
    public CashCard(Long id, String cardNumber, BigDecimal balance, String owner) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.balance =balance == null ? null : balance.setScale(2, RoundingMode.HALF_UP);
        this.owner = owner;
    }
}