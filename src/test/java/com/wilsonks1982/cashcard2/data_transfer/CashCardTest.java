package com.wilsonks1982.cashcard2.data_transfer;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CashCardTest {



    @Test
    void testCashCardCreation() {
        CashCard card = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.00), "test");

        assertEquals(1L, card.id());
        assertEquals("1234567890", card.cardNumber());
        assertEquals(BigDecimal.valueOf(10000, 2), card.balance());
    }

    @Test
    void testCashCardToString() {
        CashCard card = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.00), "test");
        String expectedString = "CashCard[id=1, cardNumber=1234567890, balance=100.00, owner=test]";
        assertEquals(expectedString, card.toString());
    }

    @Test
    void testCashCardEqualsAndHashCode() {
        CashCard card1 = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.00), "test");
        CashCard card2 = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.00), "test");
        CashCard card3 = new CashCard(2L, "0987654321", BigDecimal.valueOf(50.00), "test");

        assertEquals(card1, card2);
        assertNotEquals(card1, card3);
        assertNotEquals(card2, card3);

        assertEquals(card1.hashCode(), card2.hashCode());
        assertNotEquals(card1.hashCode(), card3.hashCode());
    }


}