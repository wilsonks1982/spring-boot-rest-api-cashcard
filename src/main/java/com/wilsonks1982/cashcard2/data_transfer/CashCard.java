package com.wilsonks1982.cashcard2.data_transfer;

import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record CashCard(@Id Long id, String cardNumber, BigDecimal balance, String owner) {
    public CashCard(Long id, String cardNumber, BigDecimal balance, String owner) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.balance = balance != null ? balance.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.owner = owner;
    }
}