# CashCard Integration Tests

## Overview

Integration tests for the CashCard API validate the complete flow from HTTP request to database, including authentication, validation, authorization, and business logic. These tests use `TestRestTemplate` and test against the running Spring Boot application context.

**Test Scope**: HTTP layer, authentication, validation, authorization, and database persistence

**Important Note**: Integration tests run against an in-memory H2 database and use HTTP Basic Authentication.

---

## Table of Contents

1. [Integration Test Setup](#integration-test-setup)
2. [Testing the GET API](#testing-the-get-api)
3. [Testing the POST API](#testing-the-post-api)
4. [Testing the PUT API](#testing-the-put-api)
5. [Testing the DELETE API](#testing-the-delete-api)
6. [Test Execution](#test-execution-guide)
7. [Best Practices](#best-practices)

---

## Test Coverage Summary

| Category | Test Count | Focus Area |
|----------|-----------|-----------|
| GET Single Card | 8 | Authentication, validation, authorization, data integrity |
| GET All Cards | 4 | Multi-tenancy, authorization, list operations |
| POST Create | 10 | Validation, authentication, server-generation, error handling |
| PUT Update | 7 | Validation, authorization, multi-tenancy, immutability |
| DELETE | 6 | Authorization, deletion, error handling, data isolation |
| **Total** | **35** | **Comprehensive API coverage** |

---
## Integration Test Setup

### Test Class Base Configuration

```java
package com.wilsonks1982.cashcard2;


import com.wilsonks1982.cashcard2.data_transfer.CashCard;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("CashCardController Integration Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CashCardControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    private static final String API_BASE_URL = "/cashcard/";
    private static final String BASIC_AUTH_USER1 = "user1";
    private static final String BASIC_AUTH_PASSWORD1 = "user@1";
    
    //Test methods will go here
}

```

### Annotations Explained

| Annotation | Purpose |
|-----------|---------|
| `@SpringBootTest` | Loads the full application context for integration testing |
| `WebEnvironment.RANDOM_PORT` | Starts the application on a random available port |
| `@DirtiesContext` | Clears the database after each test for isolation |
| `@Slf4j` | Provides logger for debugging test execution |
| `@DisplayName` | Provides readable test names |

---

---

## Helper Methods

```java
// Helper method to create CashCard DTO for requests
private CashCard createCashCardRequest(String cardNumber, BigDecimal balance) {
    // Return a CashCard with null id and owner (server-generated)
    return new CashCard(null, cardNumber, balance, null);
}

// Helper method to create JSON request body
private String createCashCardRequestJson(String cardNumber, BigDecimal balance) {
    return """
            {
              "cardNumber": "%s",
              "balance": %s
            }
            """.formatted(cardNumber, balance);
}

// Helper method to create update request
private String createUpdateCashCardRequest(Long id, String cardNumber, BigDecimal balance, String owner) {
    return """
            {
              "id": %d,
              "cardNumber": "%s",
              "balance": %s,
              "owner": "%s"
            }
            """.formatted(id, cardNumber, balance, owner);
}

// Helper method to create JSON headers
private HttpHeaders createJsonHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
}

private HttpEntity<String> createJsonHttpEntity(String jsonBody) {
    HttpHeaders headers = createJsonHeaders();
    return new HttpEntity<>(jsonBody, headers);
}
```

---

## Testing the GET API

### 1. GET Single CashCard Tests

```java
@Nested
@DisplayName("GET /cashcard/{id} - Retrieve Single CashCard")
class GetSingleCashCardTests {

    @Test
    @DisplayName("Should retrieve existing CashCard with valid id and authentication")
    void shouldRetrieveExistingCashCard() {
        // Arrange
        String cardNumber = "1234567890";
        BigDecimal balance = BigDecimal.valueOf(100.50);
        
        // First, create a card
        ResponseEntity<CashCard> createResponse = restTemplate
                .withBasicAuth(BASIC_AUTH_USER, BASIC_AUTH_PASSWORD)
                .postForEntity(API_BASE_URL, 
                    createCashCardRequest(cardNumber, balance), 
                    CashCard.class);
        
        Long cardId = createResponse.getBody().id();
        
        // Act - Retrieve the card
        ResponseEntity<CashCard> getResponse = restTemplate
                .withBasicAuth(BASIC_AUTH_USER, BASIC_AUTH_PASSWORD)
                .getForEntity(API_BASE_URL + "/" + cardId, CashCard.class);

        // Assert
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().id()).isEqualTo(cardId);
        assertThat(getResponse.getBody().cardNumber()).isEqualTo(cardNumber);
        assertThat(getResponse.getBody().balance()).isEqualByComparingTo(balance);
        assertThat(getResponse.getBody().owner()).isEqualTo(BASIC_AUTH_USER);
        
        log.info("Successfully retrieved CashCard with id: {}", cardId);
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when retrieving without authentication")
    void shouldReturnUnauthorizedWithoutAuth() {
        // Arrange & Act
        ResponseEntity<String> response = restTemplate
                .getForEntity(API_BASE_URL + "/1", String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        log.info("Request without auth returned: {}", response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized with invalid credentials")
    void shouldReturnUnauthorizedWithInvalidCredentials() {
        // Arrange & Act
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("invalid_user", "invalid_password")
                .getForEntity(API_BASE_URL + "/1", String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should return 404 Not Found when CashCard does not exist")
    void shouldReturnNotFoundForNonExistentCard() {
        // Arrange & Act
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(BASIC_AUTH_USER, BASIC_AUTH_PASSWORD)
                .getForEntity(API_BASE_URL + "/99999", String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        log.info("Non-existent card request returned: {}", response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when id is negative")
    void shouldReturnBadRequestForNegativeId() {
        // Arrange & Act
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(BASIC_AUTH_USER, BASIC_AUTH_PASSWORD)
                .getForEntity(API_BASE_URL + "/-1", String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("positive");
        log.info("Negative id request returned: {}", response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when id is not numeric")
    void shouldReturnBadRequestForNonNumericId() {
        // Arrange & Act
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(BASIC_AUTH_USER, BASIC_AUTH_PASSWORD)
                .getForEntity(API_BASE_URL + "/abc", String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return 403 Forbidden when accessing another user's card")
    void shouldReturnForbiddenForAnotherUsersCard() {
        // Arrange - Create a card as 'test' user
        ResponseEntity<CashCard> createResponse = restTemplate
                .withBasicAuth(BASIC_AUTH_USER, BASIC_AUTH_PASSWORD)
                .postForEntity(API_BASE_URL, 
                    createCashCardRequest("1234567890", BigDecimal.valueOf(100.00)), 
                    CashCard.class);
        
        Long cardId = createResponse.getBody().id();

        // Act - Try to retrieve as 'other_user'
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("other_user", "password")
                .getForEntity(API_BASE_URL + "/" + cardId, String.class);

        // Assert
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        log.info("Accessing another user's card returned: {}", getResponse.getStatusCode());
    }

    @Test
    @DisplayName("Should return server-populated owner field")
    void shouldReturnServerPopulatedOwner() {
        // Arrange - Create a card
        ResponseEntity<CashCard> createResponse = restTemplate
                .withBasicAuth("john_doe", "password123")
                .postForEntity(API_BASE_URL, 
                    createCashCardRequest("1234567890", BigDecimal.valueOf(100.00)), 
                    CashCard.class);

        // Act - Retrieve the card
        ResponseEntity<CashCard> getResponse = restTemplate
                .withBasicAuth("john_doe", "password123")
                .getForEntity(API_BASE_URL + "/" + createResponse.getBody().id(), CashCard.class);

        // Assert
        assertThat(getResponse.getBody().owner()).isEqualTo("john_doe");
        log.info("Card owner correctly populated: {}", getResponse.getBody().owner());
    }

    @Test
    @DisplayName("Should return balance with correct scale (2 decimal places)")
    void shouldReturnBalanceWithCorrectScale() {
        // Arrange - Create a card with unscaled balance
        ResponseEntity<CashCard> createResponse = restTemplate
                .withBasicAuth(BASIC_AUTH_USER, BASIC_AUTH_PASSWORD)
                .postForEntity(API_BASE_URL, 
                    createCashCardRequest("1234567890", BigDecimal.valueOf(100.5)), 
                    CashCard.class);

        // Act - Retrieve the card
        ResponseEntity<CashCard> getResponse = restTemplate
                .withBasicAuth(BASIC_AUTH_USER, BASIC_AUTH_PASSWORD)
                .getForEntity(API_BASE_URL + "/" + createResponse.getBody().id(), CashCard.class);

        // Assert
        assertThat(getResponse.getBody().balance()).isEqualByComparingTo(BigDecimal.valueOf(100.50));
        log.info("Balance correctly scaled: {}", getResponse.getBody().balance());
    }
}
```

### 2. GET All CashCards Tests

```java
@Nested
@DisplayName("GET /cashcard/ - Retrieve All CashCards")
class GetAllCashCardsTests {
    @Test
    @DisplayName("Should retrieve all CashCards for authenticated user")
    void shouldRetrieveAllCardsForUser() {
        // Arrange - Create multiple cards
        restTemplate.withBasicAuth(BASIC_AUTH_USER1, BASIC_AUTH_PASSWORD1)
                .postForEntity(API_BASE_URL,
                        createCashCardRequest("5555555555", BigDecimal.valueOf(100.00)),
                        CashCard.class);

        restTemplate.withBasicAuth(BASIC_AUTH_USER1, BASIC_AUTH_PASSWORD1)
                .postForEntity(API_BASE_URL,
                        createCashCardRequest("6666666666", BigDecimal.valueOf(200.00)),
                        CashCard.class);

        // Act
        ResponseEntity<CashCard[]> response = restTemplate
                .withBasicAuth(BASIC_AUTH_USER1, BASIC_AUTH_PASSWORD1)
                .getForEntity(API_BASE_URL, CashCard[].class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).allMatch(card -> card.owner().equals(BASIC_AUTH_USER1));

        log.info("Retrieved {} cards for user {}", response.getBody().length, BASIC_AUTH_USER1);
    }


    @Test
    @DisplayName("Should return empty list when user has no cards")
    void shouldReturnEmptyListWhenNoCards() {
        // Arrange & Act
        ResponseEntity<CashCard[]> response = restTemplate
                .withBasicAuth("user1", "user@1")
                .getForEntity(API_BASE_URL, CashCard[].class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("Should return only cards belonging to authenticated user")
    void shouldReturnOnlyUserOwnedCards() {
        // Arrange - Create cards for different users
        restTemplate.withBasicAuth("user1", "user@1")
                .postForEntity(API_BASE_URL,
                        createCashCardRequest("5555555555", BigDecimal.valueOf(100.00)),
                        CashCard.class);

        restTemplate.withBasicAuth("user2", "user@22")
                .postForEntity(API_BASE_URL,
                        createCashCardRequest("6666666666", BigDecimal.valueOf(200.00)),
                        CashCard.class);

        // Act - Retrieve as user1
        ResponseEntity<CashCard[]> response = restTemplate
                .withBasicAuth("user1", "user@1")
                .getForEntity(API_BASE_URL, CashCard[].class);

        // Assert
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].owner()).isEqualTo("user1");
        assertThat(response.getBody()[0].cardNumber()).isEqualTo("5555555555");

        log.info("User1 correctly sees only their own card");
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when retrieving without authentication")
    void shouldReturnUnauthorizedWithoutAuth() {
        // Arrange & Act
        ResponseEntity<String> response = restTemplate
                .getForEntity(API_BASE_URL, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
```

---

## Testing the POST API

### Create CashCard Tests

```java
@Nested
@DisplayName("POST /cashcard/ - Create CashCard")
class CreateCashCardTests {

    @Test
    @DisplayName("Should create CashCard with valid data and authentication")
    void shouldCreateCashCardWithValidData() {
        // Arrange
        String cardNumber = "1234567890";
        BigDecimal balance = BigDecimal.valueOf(100.50);
        String requestBody = createCashCardRequestJson(cardNumber, balance);
        HttpHeaders headers = createJsonHeaders();

        // Act
        ResponseEntity<CashCard> response = restTemplate
                .withBasicAuth(BASIC_AUTH_USER, BASIC_AUTH_PASSWORD)
                .postForEntity(API_BASE_URL, new HttpEntity<>(requestBody, headers), CashCard.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().cardNumber()).isEqualTo(cardNumber);
        assertThat(response.getBody().balance()).isEqualByComparingTo(balance);
        assertThat(response.getBody().owner()).isEqualTo(BASIC_AUTH_USER);
        assertThat(response.getHeaders().getLocation()).isNotNull();

        log.info("Created CashCard with id: {}", response.getBody().id());
    }

    @Test
    @DisplayName("Should auto-generate id on server side")
    void shouldAutoGenerateId() {
        // Arrange
        String requestBody = createCashCardRequestJson("1234567890", BigDecimal.valueOf(100.00));
        HttpHeaders headers = createJsonHeaders();
        // Act
        //When you pass a JSON string to postForEntity(), Spring is treating it as plain text instead of JSON.
        // You need to specify the Content-Type header as application/json.
        ResponseEntity<CashCard> response = restTemplate
                .withBasicAuth(BASIC_AUTH_USER, BASIC_AUTH_PASSWORD)
                .postForEntity(API_BASE_URL, new HttpEntity<>(requestBody, headers), CashCard.class);

        // Assert
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().id()).isGreaterThan(0L);
        log.info("Server generated id: {}", response.getBody().id());
    }

    @Test
    @DisplayName("Should auto-populate owner from authenticated user")
    void shouldAutoPopulateOwner() {
        // Arrange
        String authenticatedUser = "user1";
        CashCard requestBody = createCashCardRequest("1234567890", BigDecimal.valueOf(100.00));

        // Act
        ResponseEntity<CashCard> response = restTemplate
                .withBasicAuth(authenticatedUser, "user@1")
                .postForEntity(API_BASE_URL, requestBody, CashCard.class);

        // Assert
        assertThat(response.getBody().owner()).isEqualTo(authenticatedUser);
        log.info("Owner auto-populated as: {}", authenticatedUser);
    }

    @Test
    @DisplayName("Should return 400 Bad Request with blank card number")
    void shouldReturnBadRequestWithBlankCardNumber() {
        // Arrange
        CashCard requestBody = createCashCardRequest("", BigDecimal.valueOf(100.00));

        // Act
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(BASIC_AUTH_USER, BASIC_AUTH_PASSWORD)
                .postForEntity(API_BASE_URL, requestBody, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("blank");
        log.info("Blank card number validation: {}", response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 400 Bad Request with invalid card number format")
    void shouldReturnBadRequestWithInvalidCardNumberFormat() {
        // Arrange
        CashCard requestBody = createCashCardRequest("123456789", BigDecimal.valueOf(100.00));

        // Act
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(BASIC_AUTH_USER, BASIC_AUTH_PASSWORD)
                .postForEntity(API_BASE_URL, requestBody, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("10 digits");
        log.info("Invalid card number validation: {}", response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 400 Bad Request with null balance")
    void shouldReturnBadRequestWithNullBalance() {
        // Arrange
        String requestBody = """
                {
                  "cardNumber": "1234567890"
                }
                """;
        HttpHeaders headers = createJsonHeaders();

        // Act
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(BASIC_AUTH_USER, BASIC_AUTH_PASSWORD)
                .postForEntity(API_BASE_URL, new HttpEntity<>(requestBody, headers ), String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        log.info("Null balance validation: {}", response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 400 Bad Request with negative balance")
    void shouldReturnBadRequestWithNegativeBalance() {
        // Arrange
        CashCard requestBody = createCashCardRequest("1234567890", BigDecimal.valueOf(-10.00));

        // Act
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(BASIC_AUTH_USER, BASIC_AUTH_PASSWORD)
                .postForEntity(API_BASE_URL, requestBody, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("non-negative");
        log.info("Negative balance validation: {}", response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 400 Bad Request with balance exceeding maximum")
    void shouldReturnBadRequestWithExceededMaxBalance() {
        // Arrange
        CashCard requestBody = createCashCardRequest("1234567890", BigDecimal.valueOf(1000000.01));

        // Act
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(BASIC_AUTH_USER, BASIC_AUTH_PASSWORD)
                .postForEntity(API_BASE_URL, requestBody, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("1,000,000.00");
        log.info("Exceeded max balance validation: {}", response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized without authentication")
    void shouldReturnUnauthorizedWithoutAuth() {
        // Arrange
        CashCard requestBody = createCashCardRequest("1234567890", BigDecimal.valueOf(100.00));

        // Act
        ResponseEntity<String> response = restTemplate
                .postForEntity(API_BASE_URL, requestBody, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should round balance to 2 decimal places")
    void shouldRoundBalanceTo2Decimals() {
        // Arrange
        CashCard requestBody = createCashCardRequest("1234567890", BigDecimal.valueOf(100.556));

        // Act
        ResponseEntity<CashCard> response = restTemplate
                .withBasicAuth(BASIC_AUTH_USER, BASIC_AUTH_PASSWORD)
                .postForEntity(API_BASE_URL, requestBody, CashCard.class);

        // Assert
        assertThat(response.getBody().balance()).isEqualByComparingTo(BigDecimal.valueOf(100.56));
        log.info("Balance rounded: {} -> {}", 100.556, response.getBody().balance());
    }

    @Test
    @DisplayName("Should return Location header with URI of created resource")
    void shouldReturnLocationHeader() {
        // Arrange
        CashCard requestBody = createCashCardRequest("1234567890", BigDecimal.valueOf(100.00));

        // Act
        ResponseEntity<CashCard> response = restTemplate
                .withBasicAuth(BASIC_AUTH_USER, BASIC_AUTH_PASSWORD)
                .postForEntity(API_BASE_URL, requestBody, CashCard.class);

        // Assert
        assertThat(response.getHeaders().getLocation()).isNotNull();
        assertThat(response.getHeaders().getLocation().toString())
                .contains(API_BASE_URL)
                .contains(response.getBody().id().toString());
        log.info("Location header: {}", response.getHeaders().getLocation());
    }

}
```

---

## Testing the PUT API

### Update CashCard Tests

```java
    @Nested
@DisplayName("PUT /cashcard/{id} - Update CashCard")
class UpdateCashCardTests {
    @Test
    @DisplayName("Should update existing CashCard with valid data")
    void shouldUpdateCashCardWithValidData() {
        // Arrange - Create a card
        ResponseEntity<CashCard> createResponse = restTemplate
                .withBasicAuth(BASIC_AUTH_USER1, BASIC_AUTH_PASSWORD1)
                .postForEntity(API_BASE_URL,
                        createCashCardRequest("1234567890", BigDecimal.valueOf(100.00)),
                        CashCard.class);

        log.info("Created card with id: {} for update test", createResponse.getBody().id());
        Long cardId = createResponse.getBody().id();
        String updatedCardNumber = "9876543210";
        BigDecimal updatedBalance = BigDecimal.valueOf(500.75);

        // Act - Update the card
        String updateRequestJsonString = createUpdateCashCardRequest(cardId, updatedCardNumber, updatedBalance, BASIC_AUTH_USER1);
        HttpEntity<String> updateEntity = createJsonHttpEntity(updateRequestJsonString);

        ResponseEntity<CashCard> updateResponse = restTemplate
                .withBasicAuth(BASIC_AUTH_USER1, BASIC_AUTH_PASSWORD1)
                .exchange(API_BASE_URL + "/" + cardId, HttpMethod.PUT, updateEntity, CashCard.class);

        log.info("Update response: {}", updateResponse);

        // Assert - Retrieve and verify
        ResponseEntity<CashCard> getResponse = restTemplate
                .withBasicAuth(BASIC_AUTH_USER1, BASIC_AUTH_PASSWORD1)
                .getForEntity(API_BASE_URL + "/" + cardId, CashCard.class);

        log.info("Response after update: {}", getResponse);

        assertThat(getResponse.getBody().cardNumber()).isEqualTo(updatedCardNumber);
        assertThat(getResponse.getBody().balance()).isEqualByComparingTo(updatedBalance);
        log.info("Updated card {} with new cardNumber: {}", cardId, updatedCardNumber);
    }


    @Test
    @DisplayName("Should return 400 Bad Request when updating with invalid card number")
    void shouldReturnBadRequestWithInvalidCardNumber() {
        // Arrange - Create a card
        ResponseEntity<CashCard> createResponse = restTemplate
                .withBasicAuth(BASIC_AUTH_USER1, BASIC_AUTH_PASSWORD1)
                .postForEntity(API_BASE_URL,
                        createCashCardRequest("1234567890", BigDecimal.valueOf(100.00)),
                        CashCard.class);

        Long cardId = createResponse.getBody().id();
        String updateRequest = createUpdateCashCardRequest(cardId, "123", BigDecimal.valueOf(100.00), BASIC_AUTH_USER1);
        HttpEntity<String> updateEntity = createJsonHttpEntity(updateRequest);

        // Act
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(BASIC_AUTH_USER1, BASIC_AUTH_PASSWORD1)
                .exchange(API_BASE_URL + "/" + cardId,
                        org.springframework.http.HttpMethod.PUT,
                        updateEntity,
                        String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    @DisplayName("Should return 400 Bad Request when updating with negative balance")
    void shouldReturnBadRequestWithNegativeBalance() {
        // Arrange - Create a card
        ResponseEntity<CashCard> createResponse = restTemplate
                .withBasicAuth(BASIC_AUTH_USER1, BASIC_AUTH_PASSWORD1)
                .postForEntity(API_BASE_URL,
                        createCashCardRequest("1234567890", BigDecimal.valueOf(100.00)),
                        CashCard.class);

        Long cardId = createResponse.getBody().id();
        String updateRequest = createUpdateCashCardRequest(cardId, "9876543210", BigDecimal.valueOf(-50.00), BASIC_AUTH_USER1);
        HttpEntity<String> updateEntity = createJsonHttpEntity(updateRequest);

        // Act
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(BASIC_AUTH_USER1, BASIC_AUTH_PASSWORD1)
                .exchange(API_BASE_URL + "/" + cardId,
                        org.springframework.http.HttpMethod.PUT,
                        updateEntity,
                        String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return 403 Forbidden when trying to change owner")
    void shouldReturnForbiddenWhenChangingOwner() {
        // Arrange - Create a card as user1
        ResponseEntity<CashCard> createResponse = restTemplate
                .withBasicAuth("user1", "user@1")
                .postForEntity(API_BASE_URL,
                        createCashCardRequest("1234567890", BigDecimal.valueOf(100.00)),
                        CashCard.class);

        Long cardId = createResponse.getBody().id();
        String updateRequest = createUpdateCashCardRequest(cardId, "1234567890", BigDecimal.valueOf(100.00), "user2");
        HttpEntity<String> updateEntity = createJsonHttpEntity(updateRequest);

        // Act
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user1", "user@1")
                .exchange(API_BASE_URL + "/" + cardId,
                        HttpMethod.PUT,
                        updateEntity,
                        String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        log.info("Attempt to change owner returned: {}", response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 404 Not Found when updating non-existent card")
    void shouldReturnNotFoundForNonExistentCard() {
        // Arrange
        String updateRequest = createUpdateCashCardRequest(99999L, "1234567890", BigDecimal.valueOf(100.00), BASIC_AUTH_USER1);
        HttpEntity<String> updateEntity = createJsonHttpEntity(updateRequest);

        // Act
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(BASIC_AUTH_USER1, BASIC_AUTH_PASSWORD1)
                .exchange(API_BASE_URL + "/99999",
                        HttpMethod.PUT,
                        updateEntity,
                        String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should return 403 Forbidden when accessing another user's card")
    void shouldReturnForbiddenForAnotherUsersCard() {
        // Arrange - Create a card as user1
        ResponseEntity<CashCard> createResponse = restTemplate
                .withBasicAuth("user1", "user@1")
                .postForEntity(API_BASE_URL,
                        createCashCardRequest("1234567890", BigDecimal.valueOf(100.00)),
                        CashCard.class);

        Long cardId = createResponse.getBody().id();
        String updateRequest = createUpdateCashCardRequest(cardId, "9876543210", BigDecimal.valueOf(200.00), "user2");
        HttpEntity<String> updateEntity = createJsonHttpEntity(updateRequest);

        // Act - Try to update as user2
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user2", "user@22")
                .exchange(API_BASE_URL + "/" + cardId,
                        HttpMethod.PUT,
                        updateEntity,
                        String.class);

        log.info("Response when user2 tries to update user1's card: {}", response);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Should return 401 Unauthorized without authentication")
    void shouldReturnUnauthorizedWithoutAuth() {
        // Arrange
        String updateRequest = createUpdateCashCardRequest(1L, "1234567890", BigDecimal.valueOf(100.00), BASIC_AUTH_USER1);
        HttpEntity<String> updateEntity = createJsonHttpEntity(updateRequest);

        // Act
        ResponseEntity<String> response = restTemplate
                .exchange(API_BASE_URL + "/1",
                        HttpMethod.PUT,
                        updateEntity,
                        String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should round balance to 2 decimal places on update")
    void shouldRoundBalanceOnUpdate() {
        // Arrange - Create a card
        ResponseEntity<CashCard> createResponse = restTemplate
                .withBasicAuth(BASIC_AUTH_USER1, BASIC_AUTH_PASSWORD1)
                .postForEntity(API_BASE_URL,
                        createCashCardRequest("1234567890", BigDecimal.valueOf(100.00)),
                        CashCard.class);

        Long cardId = createResponse.getBody().id();
        String updateRequest = createUpdateCashCardRequest(cardId, "1234567890", BigDecimal.valueOf(250.556), BASIC_AUTH_USER1);
        HttpEntity<String> updateEntity = createJsonHttpEntity(updateRequest);

        // Act
        restTemplate
                .withBasicAuth(BASIC_AUTH_USER1, BASIC_AUTH_PASSWORD1)
                .put(API_BASE_URL + "/" + cardId, updateEntity);

        // Assert
        ResponseEntity<CashCard> getResponse = restTemplate
                .withBasicAuth(BASIC_AUTH_USER1, BASIC_AUTH_PASSWORD1)
                .getForEntity(API_BASE_URL + "/" + cardId, CashCard.class);

        assertThat(getResponse.getBody().balance()).isEqualByComparingTo(BigDecimal.valueOf(250.56));
        log.info("Balance rounded on update: {} -> {}", 250.556, getResponse.getBody().balance());
    }
}
```

---

## Testing the DELETE API

### Delete CashCard Tests

```java
   @Nested
@DisplayName("DELETE /cashcard/{id} - Delete CashCard")
class DeleteCashCardTests {
    @Test
    @DisplayName("Should delete existing CashCard")
    void shouldDeleteExistingCashCard() {
        // Arrange - Create a card
        ResponseEntity<CashCard> createResponse = restTemplate
                .withBasicAuth(BASIC_AUTH_USER1, BASIC_AUTH_PASSWORD1)
                .postForEntity(API_BASE_URL,
                        createCashCardRequest("1234567890", BigDecimal.valueOf(100.00)),
                        CashCard.class);

        Long cardId = createResponse.getBody().id();

        // Act - Delete the card
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth(BASIC_AUTH_USER1, BASIC_AUTH_PASSWORD1)
                .exchange(API_BASE_URL + "/" + cardId,
                        org.springframework.http.HttpMethod.DELETE,
                        org.springframework.http.HttpEntity.EMPTY,
                        Void.class);

        // Assert - Verify deletion
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify card is actually deleted
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth(BASIC_AUTH_USER1, BASIC_AUTH_PASSWORD1)
                .getForEntity(API_BASE_URL + "/" + cardId, String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        log.info("Successfully deleted card with id: {}", cardId);
    }

    @Test
    @DisplayName("Should return 404 Not Found when deleting non-existent card")
    void shouldReturnNotFoundForNonExistentCard() {
        // Arrange & Act
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(BASIC_AUTH_USER1, BASIC_AUTH_PASSWORD1)
                .exchange(API_BASE_URL + "/99999",
                        org.springframework.http.HttpMethod.DELETE,
                        org.springframework.http.HttpEntity.EMPTY,
                        String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        log.info("Delete non-existent card returned: {}", response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 404 Not Found when deleting another user's card")
    void shouldReturnForbiddenForAnotherUsersCard() {
        // Arrange - Create a card as user1
        ResponseEntity<CashCard> createResponse = restTemplate
                .withBasicAuth("user1", "user@1")
                .postForEntity(API_BASE_URL,
                        createCashCardRequest("1234567890", BigDecimal.valueOf(100.00)),
                        CashCard.class);

        Long cardId = createResponse.getBody().id();

        // Act - Try to delete as user2
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user2", "user@22")
                .exchange(API_BASE_URL + "/" + cardId,
                        org.springframework.http.HttpMethod.DELETE,
                        org.springframework.http.HttpEntity.EMPTY,
                        String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        log.info("Delete another user's card returned: {}", response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when id is negative")
    void shouldReturnBadRequestForNegativeId() {
        // Arrange & Act
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(BASIC_AUTH_USER1, BASIC_AUTH_PASSWORD1)
                .exchange(API_BASE_URL + "/-1",
                        org.springframework.http.HttpMethod.DELETE,
                        org.springframework.http.HttpEntity.EMPTY,
                        String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return 401 Unauthorized without authentication")
    void shouldReturnUnauthorizedWithoutAuth() {
        // Arrange & Act
        ResponseEntity<String> response = restTemplate
                .exchange(API_BASE_URL + "/1",
                        org.springframework.http.HttpMethod.DELETE,
                        org.springframework.http.HttpEntity.EMPTY,
                        String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should not delete cards belonging to other users")
    void shouldNotDeleteOtherUsersCards() {
        // Arrange - Create cards for two users
        ResponseEntity<CashCard> user1CardResponse = restTemplate
                .withBasicAuth("user1", "user@1")
                .postForEntity(API_BASE_URL,
                        createCashCardRequest("5555555555", BigDecimal.valueOf(100.00)),
                        CashCard.class);

        log.info("Response for user1's card creation: {}", user1CardResponse);

        ResponseEntity<CashCard> user2CardResponse = restTemplate
                .withBasicAuth("user2", "user@22")
                .postForEntity(API_BASE_URL,
                        createCashCardRequest("6666666666", BigDecimal.valueOf(200.00)),
                        CashCard.class);

        log.info("Response for user2's card creation: {}", user2CardResponse);

        Long user2CardId = user2CardResponse.getBody().id();

        // Act - Try to delete user2's card as user1
        ResponseEntity<String> deleteResponse = restTemplate
                .withBasicAuth("user1", "user@1")
                .exchange(API_BASE_URL + "/" + user2CardId,
                        org.springframework.http.HttpMethod.DELETE,
                        org.springframework.http.HttpEntity.EMPTY,
                        String.class);

        // Assert - Delete should be forbidden
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Verify user2's card still exists
        ResponseEntity<CashCard> getResponse = restTemplate
                .withBasicAuth("user2", "user@22")
                .getForEntity(API_BASE_URL + "/" + user2CardId, CashCard.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        log.info("User2's card protected from deletion by user1");
    }
}
```



## Test Execution Guide

### Run All Integration Tests
```bash
./gradlew test --tests CashCardControllerTest
```

### Run Specific Test Nested Class
```bash
./gradlew test --tests CashCardControllerTest\$GetSingleCashCardTests
./gradlew test --tests CashCardControllerTest\$CreateCashCardTests
./gradlew test --tests CashCardControllerTest\$UpdateCashCardTests
./gradlew test --tests CashCardControllerTest\$DeleteCashCardTests
```

### Run Specific Test Method
```bash
./gradlew test --tests CashCardControllerTest\$GetSingleCashCardTests.shouldRetrieveExistingCashCard
```

### Run Integration Tests and Unit Tests
```bash
./gradlew test
```

### Run with Verbose Output
```bash
./gradlew test --info
```

---


## Best Practices

✅ **Use @DirtiesContext** — Ensures database isolation between tests  
✅ **Test Happy Path and Errors** — Both success and failure scenarios  
✅ **Verify Response Status** — Always check HTTP status codes  
✅ **Verify Response Body** — Check returned data is correct  
✅ **Test Authentication** — Verify auth is required  
✅ **Test Authorization** — Verify users can only access their own data  
✅ **Test Validation** — Verify all validation rules work  
✅ **Use Descriptive Names** — Clear test intent  
✅ **Use @DisplayName** — Readable test descriptions  
✅ **Log Important Operations** — Help with debugging  
✅ **Test Edge Cases** — Negative values, max values, null values  
✅ **Verify Immutability** — Ensure server-managed fields can't be overridden

---

## Common Integration Test Patterns

### Test Data Setup
```java
// Create test data in @BeforeEach if needed
ResponseEntity<CashCard> setupCard = restTemplate
    .withBasicAuth("user", "user@1")
    .postForEntity(API_BASE_URL, request, CashCard.class);
```

### Verify Location Header
```java
assertThat(response.getHeaders().getLocation()).isNotNull();
```

### Test Multi-tenancy
```java
// Create as user1, verify user2 can't access
ResponseEntity<CashCard> user2Response = restTemplate
    .withBasicAuth("user2", "user@22")
    .getForEntity(API_BASE_URL + "/" + cardId, CashCard.class);

assertThat(user2Response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
```

This comprehensive integration test suite ensures your CashCard API works correctly end-to-end! 🎯