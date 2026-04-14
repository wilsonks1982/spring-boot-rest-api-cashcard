package com.wilsonks1982.cashcard2.data_transfer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CashCardJsonTest {


    //What is JacksonTester Class?
    // It is a Spring Boot test utility that provides a convenient way to test JSON serialization and deserialization
    // of Java objects using the Jackson library. It allows you to write tests that verify how your Java objects are
    // converted to JSON and vice versa, ensuring that the JSON structure matches your expectations.
    @Autowired private JacksonTester<CashCard> cashCardJacksonTester;

    @Autowired private JacksonTester<List<CashCard>> cashCardListJacksonTester;

    @Nested
    class JsonSerializationTests {

        @Test
        void testCashCardJsonSerialization() throws Exception {
            CashCard card = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.00), "jackson");
            String jsonString = cashCardJacksonTester.write(card).getJson();

            System.out.println("Serialized JSON: " + jsonString);
            assertThat(jsonString).isEqualToIgnoringWhitespace("""
                    {
                        "id": 1,
                        "cardNumber": "1234567890",
                        "balance": 100.00,
                        "owner": "jackson"
                    }
                    """);

            assertThat(cashCardJacksonTester.write(card)).isStrictlyEqualToJson("""
                    {
                        "id": 1,
                        "cardNumber": "1234567890",
                        "balance": 100.00,
                        "owner": "jackson"
                    }
                    """);

            assertThat(cashCardJacksonTester.write(card)).isStrictlyEqualToJson("cashcard_single.json");

        }


        @Test
        void testCashCardListJsonSerialization() throws Exception {
            CashCard card1 = new CashCard(1L, "1234567890", BigDecimal.valueOf(100.00), "jackson");
            CashCard card2 = new CashCard(2L, "0987654321", BigDecimal.valueOf(50.00), "jackson");
            String jsonString = cashCardListJacksonTester.write(List.of(card1, card2)).getJson();

            System.out.println("Serialized JSON List: " + jsonString);
            assertThat(jsonString).isEqualToIgnoringWhitespace("""
                    [
                        {
                            "id": 1,
                            "cardNumber": "1234567890",
                            "balance": 100.00,
                            "owner": "jackson"
                        },
                        {
                            "id": 2,
                            "cardNumber": "0987654321",
                            "balance": 50.00,
                            "owner": "jackson"
                        }
                    ]
                    """);

            assertThat(cashCardListJacksonTester.write(List.of(card1, card2))).isStrictlyEqualToJson("""
                    [
                        {
                            "id": 1,
                            "cardNumber": "1234567890",
                            "balance": 100.00,
                            "owner": "jackson"
                        },
                        {
                            "id": 2,
                            "cardNumber": "0987654321",
                            "balance": 50.00,
                            "owner": "jackson"
                        }
                    ]
                    """);

            assertThat(cashCardListJacksonTester.write(List.of(card1, card2))).isStrictlyEqualToJson("cashcard_list.json");
        }

    }

    @Nested
    class JsonDeserializationTests {
        @Test
        void testCashCardJsonDeserialization() throws Exception {
            String jsonString = """
                    {
                        "id": 1,
                        "cardNumber": "1234567890",
                        "balance": 100.0,
                        "owner": "jackson"
                    }
                    """;


            assertThat(cashCardJacksonTester.parse(jsonString)).isEqualTo(new CashCard(1L, "1234567890", BigDecimal.valueOf(100.00), "jackson"));

            assertThat(cashCardJacksonTester.parse(jsonString)).usingRecursiveComparison().isEqualTo(new CashCard(1L, "1234567890", BigDecimal.valueOf(100.00), "jackson"));

            assertThat(cashCardJacksonTester.parse(jsonString)).usingRecursiveComparison().ignoringFields("id").isEqualTo(new CashCard(null, "1234567890", BigDecimal.valueOf(100.00), "jackson"));
        }


        @Test
        void testCashCardListJsonDeserialization() throws Exception {
            String jsonString = """
                    [
                        {
                            "id": 1,
                            "cardNumber": "1234567890",
                            "balance": 100.00,
                            "owner": "jackson"
                        },
                        {
                            "id": 2,
                            "cardNumber": "0987654321",
                            "balance": 50.00,
                            "owner": "jackson"
                        }
                    ]
                    """;

            List<CashCard> expectedCards = List.of(
                    new CashCard(1L, "1234567890", BigDecimal.valueOf(10000, 2), "jackson"),
                    new CashCard(2L, "0987654321", BigDecimal.valueOf(5000, 2), "jackson")
            );

            assertThat(cashCardListJacksonTester.parse(jsonString)).isEqualTo(expectedCards);

            assertThat(cashCardListJacksonTester.parse(jsonString)).usingRecursiveComparison().isEqualTo(expectedCards);

            assertThat(cashCardListJacksonTester.parse(jsonString)).usingRecursiveComparison().ignoringFields("id").isEqualTo(List.of(
                    new CashCard(null, "1234567890", BigDecimal.valueOf(100.00), "jackson"),
                    new CashCard(null, "0987654321", BigDecimal.valueOf(50.00), "jackson")
            ));
        }

    }


}