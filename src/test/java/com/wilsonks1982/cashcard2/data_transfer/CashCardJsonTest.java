package com.wilsonks1982.cashcard2.data_transfer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JsonTest
@DisplayName("CashCard JSON Serialization and Deserialization Tests")
class CashCardJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("JSON Serialization (Object → JSON)")
    class SerializationTests {

        @Test
        @DisplayName("Should serialize complete CashCard response with all fields")
        void shouldSerializeCompleteResponse() throws Exception {
            // Arrange
            CashCard cashCard = new CashCard(
                    1L,  // Server-generated
                    "1234567890",
                    BigDecimal.valueOf(100.50),
                    "john_doe"  // Server-populated
            );

            // Act
            String json = objectMapper.writeValueAsString(cashCard);

            // Assert
            assertThat(json).contains("\"id\":1");
            assertThat(json).contains("\"cardNumber\":\"1234567890\"");
            assertThat(json).contains("\"balance\":100.50");
            assertThat(json).contains("\"owner\":\"john_doe\"");
        }

        @Test
        @DisplayName("Should serialize server-generated id field in response")
        void shouldSerializeServerId() throws Exception {
            // Arrange
            CashCard cashCard = new CashCard(12345L, "1234567890", BigDecimal.valueOf(100.50), "john_doe");

            // Act
            String json = objectMapper.writeValueAsString(cashCard);

            // Assert
            assertThat(json).contains("\"id\":12345");
        }

        @Test
        @DisplayName("Should serialize server-populated owner field in response")
        void shouldSerializeOwner() throws Exception {
            // Arrange
            CashCard cashCard = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.50), "jane_smith");

            // Act
            String json = objectMapper.writeValueAsString(cashCard);

            // Assert
            assertThat(json).contains("\"owner\":\"jane_smith\"");
        }

        @Test
        @DisplayName("Should serialize balance with exactly 2 decimal places")
        void shouldSerializeBalanceWith2Decimals() throws Exception {
            // Arrange
            CashCard cashCard = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.50), "john_doe");

            // Act
            String json = objectMapper.writeValueAsString(cashCard);

            // Assert
            assertThat(json).contains("\"balance\":100.50");
        }

        @Test
        @DisplayName("Should serialize minimum balance correctly")
        void shouldSerializeMinimumBalance() throws Exception {
            // Arrange
            CashCard cashCard = new CashCard(1L, "1234567890", BigDecimal.valueOf(0.00), "john_doe");

            // Act
            String json = objectMapper.writeValueAsString(cashCard);

            // Assert
            assertThat(json).contains("\"balance\":0.00");
        }

        @Test
        @DisplayName("Should serialize maximum balance correctly")
        void shouldSerializeMaximumBalance() throws Exception {
            // Arrange
            CashCard cashCard = new CashCard(1L, "1234567890", BigDecimal.valueOf(1000000.00), "john_doe");

            // Act
            String json = objectMapper.writeValueAsString(cashCard);

            // Assert
            assertThat(json).contains("\"balance\":1000000.00");
        }

        @Test
        @DisplayName("Should serialize card number as string value")
        void shouldSerializeCardNumberAsString() throws Exception {
            // Arrange
            CashCard cashCard = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.50), "john_doe");

            // Act
            String json = objectMapper.writeValueAsString(cashCard);

            // Assert
            assertThat(json).contains("\"cardNumber\":\"1234567890\"");
        }
    }

    @Nested
    @DisplayName("JSON Deserialization (JSON → Object)")
    class DeserializationTests {

        @Test
        @DisplayName("Should deserialize complete server response JSON")
        void shouldDeserializeCompleteResponse() throws Exception {
            // Arrange - Server response includes all fields
            String json = """
                {
                  "id": 1,
                  "cardNumber": "1234567890",
                  "balance": 100.50,
                  "owner": "john_doe"
                }
                """;

            // Act
            CashCard cashCard = objectMapper.readValue(json, CashCard.class);

            // Assert
            assertThat(cashCard).isNotNull();
            assertThat(cashCard.id()).isEqualTo(1L);
            assertThat(cashCard.cardNumber()).isEqualTo("1234567890");
            assertThat(cashCard.balance()).isEqualByComparingTo(BigDecimal.valueOf(100.50));
            assertThat(cashCard.owner()).isEqualTo("john_doe");
        }

        @Test
        @DisplayName("Should deserialize balance without trailing zeros")
        void shouldDeserializeBalanceWithoutTrailingZeros() throws Exception {
            // Arrange
            String json = """
                {
                  "id": 1,
                  "cardNumber": "1234567890",
                  "balance": 100.5,
                  "owner": "john_doe"
                }
                """;

            // Act
            CashCard cashCard = objectMapper.readValue(json, CashCard.class);

            // Assert - Rounded to 2 decimal places in constructor
            assertThat(cashCard.balance()).isEqualByComparingTo(BigDecimal.valueOf(100.50));
        }

        @Test
        @DisplayName("Should deserialize and auto-round balance with 3 decimal places")
        void shouldDeserializeAndRoundBalance() throws Exception {
            // Arrange
            String json = """
                {
                  "id": 1,
                  "cardNumber": "1234567890",
                  "balance": 100.556,
                  "owner": "john_doe"
                }
                """;

            // Act
            CashCard cashCard = objectMapper.readValue(json, CashCard.class);

            // Assert - Should be rounded to 100.56 in constructor
            assertThat(cashCard.balance()).isEqualByComparingTo(BigDecimal.valueOf(100.56));
        }

        @Test
        @DisplayName("Should deserialize large id values")
        void shouldDeserializeLargeId() throws Exception {
            // Arrange
            String json = """
                {
                  "id": 9223372036854775807,
                  "cardNumber": "1234567890",
                  "balance": 100.50,
                  "owner": "john_doe"
                }
                """;

            // Act
            CashCard cashCard = objectMapper.readValue(json, CashCard.class);

            // Assert
            assertThat(cashCard.id()).isEqualTo(9223372036854775807L);
        }

        @Test
        @DisplayName("Should deserialize special characters in owner")
        void shouldDeserializeSpecialCharactersInOwner() throws Exception {
            // Arrange
            String json = """
                {
                  "id": 1,
                  "cardNumber": "1234567890",
                  "balance": 100.50,
                  "owner": "john_doe_123@domain"
                }
                """;

            // Act
            CashCard cashCard = objectMapper.readValue(json, CashCard.class);

            // Assert
            assertThat(cashCard.owner()).isEqualTo("john_doe_123@domain");
        }
    }

    @Nested
    @DisplayName("JSON Round-trip Tests (Serialization ↔ Deserialization)")
    class RoundTripTests {

        @Test
        @DisplayName("Should serialize and deserialize CashCard without data loss")
        void shouldRoundTripWithoutDataLoss() throws Exception {
            // Arrange
            CashCard originalCard = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.50), "john_doe");

            // Act
            String json = objectMapper.writeValueAsString(originalCard);
            CashCard deserializedCard = objectMapper.readValue(json, CashCard.class);

            // Assert
            assertThat(deserializedCard).isEqualTo(originalCard);
            assertThat(deserializedCard.id()).isEqualTo(originalCard.id());
            assertThat(deserializedCard.cardNumber()).isEqualTo(originalCard.cardNumber());
            assertThat(deserializedCard.balance()).isEqualByComparingTo(originalCard.balance());
            assertThat(deserializedCard.owner()).isEqualTo(originalCard.owner());
        }

        @Test
        @DisplayName("Should maintain balance precision during round-trip")
        void shouldMaintainBalancePrecision() throws Exception {
            // Arrange
            CashCard originalCard = new CashCard(1L, "1234567890", BigDecimal.valueOf(123.45), "john_doe");

            // Act
            String json = objectMapper.writeValueAsString(originalCard);
            CashCard roundTrippedCard = objectMapper.readValue(json, CashCard.class);

            // Assert
            assertThat(roundTrippedCard.balance()).isEqualByComparingTo(BigDecimal.valueOf(123.45));
            assertThat(roundTrippedCard.balance().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should handle multiple CashCard round-trips")
        void shouldHandleMultipleRoundTrips() throws Exception {
            // Arrange
            CashCard card1 = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.50), "john_doe");
            CashCard card2 = new CashCard(2L, "0987654321", BigDecimal.valueOf(500.75), "jane_smith");

            // Act
            String json1 = objectMapper.writeValueAsString(card1);
            String json2 = objectMapper.writeValueAsString(card2);

            CashCard result1 = objectMapper.readValue(json1, CashCard.class);
            CashCard result2 = objectMapper.readValue(json2, CashCard.class);

            // Assert
            assertThat(result1).isEqualTo(card1);
            assertThat(result2).isEqualTo(card2);
        }
    }
    @Nested
    @DisplayName("JSON Field Validation")
    class JsonFieldValidationTests {


        @Test
        @DisplayName("Should fail when id has wrong type")
        void shouldFailWhenIdWrongType() {
            // Arrange
            String json = """
                {
                  "id": "not_a_number",
                  "cardNumber": "1234567890",
                  "balance": 100.50,
                  "owner": "john_doe"
                }
                """;

            // Act & Assert
            assertThat(catchException(() -> objectMapper.readValue(json, CashCard.class)))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should fail when balance has wrong type")
        void shouldFailWhenBalanceWrongType() {
            // Arrange
            String json = """
                {
                  "id": 1,
                  "cardNumber": "1234567890",
                  "balance": "not_a_number",
                  "owner": "john_doe"
                }
                """;

            // Act & Assert
            assertThat(catchException(() -> objectMapper.readValue(json, CashCard.class)))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should ignore unknown JSON fields")
        void shouldIgnoreUnknownFields() throws Exception {
            // Arrange
            String json = """
                {
                  "id": 1,
                  "cardNumber": "1234567890",
                  "balance": 100.50,
                  "owner": "john_doe",
                  "unknownField": "should be ignored",
                  "anotherUnknown": 12345
                }
                """;

            // Act
            CashCard cashCard = objectMapper.readValue(json, CashCard.class);

            // Assert
            assertThat(cashCard).isNotNull();
            assertThat(cashCard.id()).isEqualTo(1L);
        }
    }



    @Nested
    @DisplayName("JSON Format and Pretty-printing")
    class JsonFormatTests {

        @Test
        @DisplayName("Should serialize to compact JSON by default")
        void shouldSerializeToCompactJson() throws Exception {
            // Arrange
            CashCard cashCard = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.50), "john_doe");

            // Act
            String json = objectMapper.writeValueAsString(cashCard);

            // Assert
            assertThat(json)
                    .doesNotContain("\n")
                    .startsWith("{")
                    .endsWith("}");
        }

        @Test
        @DisplayName("Should serialize to pretty-printed JSON with indentation")
        void shouldSerializeToPrettyJson() throws Exception {
            // Arrange
            CashCard cashCard = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.50), "john_doe");

            // Act
            String json = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(cashCard);

            // Assert
            assertThat(json)
                    .contains("\n")
                    .contains("\"id\"")
                    .contains("\"cardNumber\"")
                    .contains("\"balance\"")
                    .contains("\"owner\"");
        }

        @Test
        @DisplayName("Should deserialize both compact and pretty-printed JSON equally")
        void shouldDeserializeBothFormats() throws Exception {
            // Arrange
            CashCard originalCard = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.50), "john_doe");

            // Act
            String compactJson = objectMapper.writeValueAsString(originalCard);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(originalCard);

            CashCard fromCompact = objectMapper.readValue(compactJson, CashCard.class);
            CashCard fromPretty = objectMapper.readValue(prettyJson, CashCard.class);

            // Assert
            assertThat(fromCompact).isEqualTo(originalCard);
            assertThat(fromPretty).isEqualTo(originalCard);
            assertThat(fromCompact).isEqualTo(fromPretty);
        }
    }
}