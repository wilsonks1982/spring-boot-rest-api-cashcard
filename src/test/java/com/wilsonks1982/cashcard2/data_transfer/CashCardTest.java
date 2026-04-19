package com.wilsonks1982.cashcard2.data_transfer;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CashCardTest {


    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        // Create validator once for all tests
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }


    @Nested
    @DisplayName("Valid CashCard Creation")
    class ValidCashCardTests {

        @Test
        @DisplayName("Should create CashCard with all required fields")
        void shouldCreateValidCashCard() {
            // Arrange
            Long serverId = 1L;  // Server-generated
            String cardNumber = "1234567890";
            BigDecimal balance = BigDecimal.valueOf(100.50);
            String owner = "john_doe";  // Server-populated from auth

            // Act
            CashCard cashCard = new CashCard(serverId, cardNumber, balance, owner);

            // Assert
            assertThat(cashCard).isNotNull();
            assertThat(cashCard.id()).isEqualTo(serverId);
            assertThat(cashCard.cardNumber()).isEqualTo(cardNumber);
            assertThat(cashCard.balance()).isEqualByComparingTo(balance);
            assertThat(cashCard.owner()).isEqualTo(owner);

            // Validate against all constraints
            Set<ConstraintViolation<CashCard>> violations = validator.validate(cashCard);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should automatically round balance to 2 decimal places in constructor")
        void shouldRoundBalanceInConstructor() {
            // Arrange
            BigDecimal inputBalance = BigDecimal.valueOf(100.556);
            BigDecimal expectedBalance = BigDecimal.valueOf(100.56);

            // Act
            CashCard cashCard = new CashCard(1L, "1234567890", inputBalance, "john_doe");

            // Assert
            assertThat(cashCard.balance()).isEqualByComparingTo(expectedBalance);
            assertThat(cashCard.balance().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should round balance using HALF_UP when exactly .5")
        void shouldRoundBalanceWithHalfUp() {
            // Arrange
            BigDecimal inputBalance = BigDecimal.valueOf(100.545);
            BigDecimal expectedBalance = BigDecimal.valueOf(100.55);

            // Act
            CashCard cashCard = new CashCard(1L, "1234567890", inputBalance, "john_doe");

            // Assert - HALF_UP rounds .5 up
            assertThat(cashCard.balance()).isEqualByComparingTo(expectedBalance);
        }

        @Test
        @DisplayName("Should accept minimum balance of 0.00")
        void shouldAcceptMinimumBalance() {
            // Arrange & Act
            CashCard cashCard = new CashCard(1L, "1234567890", BigDecimal.valueOf(0.00), "john_doe");

            // Assert
            Set<ConstraintViolation<CashCard>> violations = validator.validate(cashCard);
            assertThat(violations).isEmpty();
            assertThat(cashCard.balance()).isEqualByComparingTo(BigDecimal.valueOf(0.00));
        }

        @Test
        @DisplayName("Should accept maximum balance of 1,000,000.00")
        void shouldAcceptMaximumBalance() {
            // Arrange & Act
            CashCard cashCard = new CashCard(1L, "1234567890", BigDecimal.valueOf(1000000.00), "john_doe");

            // Assert
            Set<ConstraintViolation<CashCard>> violations = validator.validate(cashCard);
            assertThat(violations).isEmpty();
            assertThat(cashCard.balance()).isEqualByComparingTo(BigDecimal.valueOf(1000000.00));
        }

        @Test
        @DisplayName("Should preserve server-generated id")
        void shouldPreserveServerId() {
            // Arrange
            Long serverId = 12345L;

            // Act
            CashCard cashCard = new CashCard(serverId, "1234567890", BigDecimal.valueOf(100.50), "john_doe");

            // Assert
            assertThat(cashCard.id()).isEqualTo(serverId);
        }

        @Test
        @DisplayName("Should preserve server-populated owner from authentication")
        void shouldPreserveServerOwner() {
            // Arrange
            String authenticatedUsername = "jane_smith";

            // Act
            CashCard cashCard = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.50), authenticatedUsername);

            // Assert
            assertThat(cashCard.owner()).isEqualTo(authenticatedUsername);
        }
    }


    @Nested
    @DisplayName("CardNumber Validation")
    class CardNumberValidationTests {

        @Test
        @DisplayName("Should reject blank card number")
        void shouldRejectBlankCardNumber() {
            // Arrange & Act
            CashCard cashCard = new CashCard(1L, "", BigDecimal.valueOf(100.00), "john_doe");

            // Assert
            Set<ConstraintViolation<CashCard>> violations = validator.validate(cashCard);
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                    v.getMessage().contains("Card number must not be blank")
            );
        }

        @Test
        @DisplayName("Should reject null card number")
        void shouldRejectNullCardNumber() {
            // Arrange & Act
            CashCard cashCard = new CashCard(1L, null, BigDecimal.valueOf(100.00), "john_doe");

            // Assert
            Set<ConstraintViolation<CashCard>> violations = validator.validate(cashCard);
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                    v.getMessage().contains("Card number must not be blank")
            );
        }

        @Test
        @DisplayName("Should reject card number with less than 10 digits")
        void shouldRejectTooFewDigits() {
            // Arrange & Act
            CashCard cashCard = new CashCard(1L, "123456789", BigDecimal.valueOf(100.00), "john_doe");

            // Assert
            Set<ConstraintViolation<CashCard>> violations = validator.validate(cashCard);
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                    v.getMessage().contains("Card number must be exactly 10 digits")
            );
        }

        @Test
        @DisplayName("Should reject card number with more than 10 digits")
        void shouldRejectTooManyDigits() {
            // Arrange & Act
            CashCard cashCard = new CashCard(1L, "12345678901", BigDecimal.valueOf(100.00), "john_doe");

            // Assert
            Set<ConstraintViolation<CashCard>> violations = validator.validate(cashCard);
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                    v.getMessage().contains("Card number must be exactly 10 digits")
            );
        }

        @Test
        @DisplayName("Should reject card number with non-digit characters")
        void shouldRejectNonDigitCharacters() {
            // Arrange & Act
            CashCard cashCard = new CashCard(1L, "123456789a", BigDecimal.valueOf(100.00), "john_doe");

            // Assert
            Set<ConstraintViolation<CashCard>> violations = validator.validate(cashCard);
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                    v.getMessage().contains("Card number must be exactly 10 digits")
            );
        }

        @Test
        @DisplayName("Should reject card number with spaces")
        void shouldRejectSpacesInCardNumber() {
            // Arrange & Act
            CashCard cashCard = new CashCard(1L, "1234 567890", BigDecimal.valueOf(100.00), "john_doe");

            // Assert
            Set<ConstraintViolation<CashCard>> violations = validator.validate(cashCard);
            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("Should accept valid card number with exactly 10 digits")
        void shouldAcceptValidCardNumber() {
            // Arrange & Act
            CashCard cashCard = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.00), "john_doe");

            // Assert
            Set<ConstraintViolation<CashCard>> violations = validator.validate(cashCard);
            assertThat(violations.stream()
                    .filter(v -> v.getMessage().contains("Card number"))
                    .toList()
            ).isEmpty();
        }

    }

    @Nested
    @DisplayName("Balance Validation")
    class BalanceValidationTests {

        @Test
        @DisplayName("Should reject null balance")
        void shouldRejectNullBalance() {
            // Arrange & Act
            CashCard cashCard = new CashCard(1L, "1234567890", null, "john_doe");

            // Assert
            Set<ConstraintViolation<CashCard>> violations = validator.validate(cashCard);
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                    v.getMessage().contains("Balance must not be null")
            );
        }

        @Test
        @DisplayName("Should reject negative balance")
        void shouldRejectNegativeBalance() {
            // Arrange & Act
            CashCard cashCard = new CashCard(1L, "1234567890", BigDecimal.valueOf(-0.01), "john_doe");

            // Assert
            Set<ConstraintViolation<CashCard>> violations = validator.validate(cashCard);
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                    v.getMessage().contains("Balance must be non-negative")
            );
        }

        @Test
        @DisplayName("Should reject balance exceeding maximum")
        void shouldRejectExceededMaximum() {
            // Arrange & Act
            CashCard cashCard = new CashCard(1L, "1234567890", BigDecimal.valueOf(1000000.01), "john_doe");

            // Assert
            Set<ConstraintViolation<CashCard>> violations = validator.validate(cashCard);
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                    v.getMessage().contains("Balance must be less than or equal to 1,000,000.00")
            );
        }

        @Test
        @DisplayName("Should round balance with 1 decimal place")
        void shouldRoundBalanceWith1Decimal() {
            // Arrange & Act
            CashCard cashCard = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.5), "john_doe");

            // Assert
            assertThat(cashCard.balance()).isEqualByComparingTo(BigDecimal.valueOf(100.50));
            assertThat(cashCard.balance().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should round balance with 3 decimal places")
        void shouldRoundBalanceWith3Decimals() {
            // Arrange & Act
            CashCard cashCard = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.555), "john_doe");

            // Assert
            assertThat(cashCard.balance()).isEqualByComparingTo(BigDecimal.valueOf(100.56));
        }

        @Test
        @DisplayName("Should handle balance as integer")
        void shouldHandleIntegerBalance() {
            // Arrange & Act
            CashCard cashCard = new CashCard(1L, "1234567890", BigDecimal.valueOf(100), "john_doe");

            // Assert
            assertThat(cashCard.balance()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        }
    }

    @Nested
    @DisplayName("CashCard Equality and Hashing")
    class EqualityTests {

        @Test
        @DisplayName("Two CashCards with identical fields should be equal")
        void shouldBeEqualWithIdenticalFields() {
            // Arrange
            CashCard card1 = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.00), "john_doe");
            CashCard card2 = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.00), "john_doe");

            // Assert
            assertThat(card1).isEqualTo(card2);
            assertThat(card1.hashCode()).isEqualTo(card2.hashCode());
        }

        @Test
        @DisplayName("Two CashCards with different id should not be equal")
        void shouldNotBeEqualWithDifferentId() {
            // Arrange
            CashCard card1 = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.00), "john_doe");
            CashCard card2 = new CashCard(2L, "1234567890", BigDecimal.valueOf(100.00), "john_doe");

            // Assert
            assertThat(card1).isNotEqualTo(card2);
        }

        @Test
        @DisplayName("Two CashCards with different cardNumber should not be equal")
        void shouldNotBeEqualWithDifferentCardNumber() {
            // Arrange
            CashCard card1 = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.00), "john_doe");
            CashCard card2 = new CashCard(1L, "9876543210", BigDecimal.valueOf(100.00), "john_doe");

            // Assert
            assertThat(card1).isNotEqualTo(card2);
        }

        @Test
        @DisplayName("Two CashCards with different balance should not be equal")
        void shouldNotBeEqualWithDifferentBalance() {
            // Arrange
            CashCard card1 = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.00), "john_doe");
            CashCard card2 = new CashCard(1L, "1234567890", BigDecimal.valueOf(200.00), "john_doe");

            // Assert
            assertThat(card1).isNotEqualTo(card2);
        }

        @Test
        @DisplayName("Two CashCards with different owner should not be equal")
        void shouldNotBeEqualWithDifferentOwner() {
            // Arrange
            CashCard card1 = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.00), "john_doe");
            CashCard card2 = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.00), "jane_doe");

            // Assert
            assertThat(card1).isNotEqualTo(card2);
        }

        @Test
        @DisplayName("CashCard should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Arrange
            CashCard card = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.00), "john_doe");

            // Assert
            assertThat(card).isNotEqualTo(null);
        }

        @Test
        @DisplayName("CashCard should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            // Arrange
            CashCard card = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.00), "john_doe");
            String notACard = "not a card";

            // Assert
            assertThat(card).isNotEqualTo(notACard);
        }
    }

    @Nested
    @DisplayName("CashCard Immutability")
    class ImmutabilityTests {

        @Test
        @DisplayName("CashCard fields are immutable (record property)")
        void shouldBeImmutable() {
            // Arrange
            CashCard card = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.00), "john_doe");

            // Assert - Records provide immutable accessors
            assertThat(card.id()).isEqualTo(1L);
            assertThat(card.cardNumber()).isEqualTo("1234567890");
            assertThat(card.balance()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
            assertThat(card.owner()).isEqualTo("john_doe");

            // Create a new instance to "update" (records are immutable)
            CashCard updatedCard = new CashCard(1L, "9876543210", BigDecimal.valueOf(200.00), "john_doe");
            assertThat(card).isNotEqualTo(updatedCard);
        }

        @Test
        @DisplayName("Server-generated id is immutable in record")
        void shouldNotModifyServerId() {
            // Arrange
            Long originalId = 1L;
            CashCard card = new CashCard(originalId, "1234567890", BigDecimal.valueOf(100.00), "john_doe");

            // Assert - No setter exists (record property)
            assertThat(card.id()).isEqualTo(originalId);

            // Create a new instance with different id
            CashCard differentCard = new CashCard(2L, "1234567890", BigDecimal.valueOf(100.00), "john_doe");
            assertThat(differentCard.id()).isNotEqualTo(originalId);
        }

        @Test
        @DisplayName("Server-populated owner is immutable in record")
        void shouldNotModifyServerOwner() {
            // Arrange
            String originalOwner = "john_doe";
            CashCard card = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.00), originalOwner);

            // Assert - No setter exists (record property)
            assertThat(card.owner()).isEqualTo(originalOwner);

            // Create a new instance with different owner
            CashCard differentCard = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.00), "jane_doe");
            assertThat(differentCard.owner()).isNotEqualTo(originalOwner);
        }
    }

}