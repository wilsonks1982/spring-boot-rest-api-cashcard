package com.wilsonks1982.cashcard2;

import com.jayway.jsonpath.DocumentContext;
import com.wilsonks1982.cashcard2.data_transfer.CashCard;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTests {

	@Autowired
	TestRestTemplate restTemplate;

	@Autowired
	ApplicationContext context;

	@Nested
	class beanTests {
		@Test
		void contextLoads() {
			assertThat(restTemplate).isNotNull();
			assertThat(context).isNotNull();
		}

		@Test
		void shouldHaveDefaultSecurityFilterChainBean() {
			log.info("Checking for defaultSecurityFilterChain bean in application context...");
			Arrays.stream(context.getBeanDefinitionNames())
					.filter(name -> name.toLowerCase().contains("filterchain"))
					.forEach(name -> log.info("Found bean: " + name));
			assertFalse(context.containsBean("defaultSecurityFilterChain"), "Expected SecurityFilterChain bean named 'defaultSecurityFilterChain' to be present in the application context");
		}
		@Test
		void shouldHaveCustomSecurityFilterChainBean() {
			log.info("Checking for customFilterChain bean in application context...");
			assertTrue(context.containsBean("customSecurityFilterChain"), "Expected SecurityFilterChain bean named 'customSecurityFilterChain' to be present in the application context");
		}

		@Test
		void shouldHavePasswordEncoderBean() {
			log.info("Checking for passwordEncoder bean in application context...");
			assertTrue(context.containsBean("passwordEncoder"), "Expected PasswordEncoder bean named 'passwordEncoder' to be present in the application context");
		}

		@Test
		void shouldHaveUserDetailsServiceBean() {
			log.info("Checking for userDetailsService bean in application context...");
			assertTrue(context.containsBean("userDetailsService"), "Expected UserDetailsService bean named 'userDetailsService' to be present in the application context");
		}
	}

	@Nested
	class getTests {

		@Test
		void shouldGetACashCardByNumber() {
			ResponseEntity<String> response = restTemplate
					.withBasicAuth("test", "abc123")
					.getForEntity("/cashcard/cardnumber/2222222222", String.class);

			log.info("Response from /cashcard/cardnumber/2222222222: " + response.toString());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isEqualToIgnoringWhitespace("""
					{
					    "id": 2,
					    "cardNumber": "2222222222",
					    "balance": 200.00,
					    "owner": "test"
					}
					""");

			DocumentContext jsonContext = com.jayway.jsonpath.JsonPath.parse(response.getBody());

			Long id = jsonContext.read("$.id", Long.class);
			String cardNumber = jsonContext.read("$.cardNumber", String.class);
			Double balance = jsonContext.read("$.balance", Double.class);

			assertThat(id).isEqualTo(2L);
			assertThat(cardNumber).isEqualTo("2222222222");
			assertThat(balance).isEqualTo(200.00);
		}

		@Test
		void shouldGetACashCardById() {
			ResponseEntity<String> response = restTemplate
					.withBasicAuth("test", "abc123")
					.getForEntity("/cashcard/1", String.class);

			log.info("Response from /cashcard/1: " + response.toString());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isEqualToIgnoringWhitespace("""
					{
					    "id": 1,
					    "cardNumber": "1111111111",
					    "balance": 100.00,
					    "owner": "test"
					}
					""");

			DocumentContext jsonContext = com.jayway.jsonpath.JsonPath.parse(response.getBody());

			Long id = jsonContext.read("$.id", Long.class);
			String cardNumber = jsonContext.read("$.cardNumber", String.class);
			Double balance = jsonContext.read("$.balance", Double.class);

			assertThat(id).isEqualTo(1L);
			assertThat(cardNumber).isEqualTo("1111111111");
			assertThat(balance).isEqualTo(100.00);
		}

		@Test
		void should404GetACashCardByIdUnKnown() {
			ResponseEntity<String> response = restTemplate
					.withBasicAuth("test", "abc123")
					.getForEntity("/cashcard/999", String.class);

			log.info("Response from /cashcard/999: " + response.toString());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
			assertThat(response.getBody()).isNullOrEmpty();
		}


		@Test
		void shouldNotReturnACashCardWhenUsingBadCredentials() {
			ResponseEntity<String> response = restTemplate
					.withBasicAuth("BAD-USER", "abc123")
					.getForEntity("/cashcard/1", String.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

			response = restTemplate
					.withBasicAuth("test", "BAD-PASSWORD")
					.getForEntity("/cashcard/1", String.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		}

		@Test
		void shouldRejectUsersWhoAreNotCardOwners() {
			ResponseEntity<String> response = restTemplate
					.withBasicAuth("visitor", "visitor123")
					.getForEntity("/cashcard/1", String.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		}
	}

	@Nested
	class postTests {
		@Test
		@DirtiesContext
		void shouldCreateANewCashCard() {
			CashCard newCard = new CashCard(null, "5555555555", BigDecimal.valueOf(500.00), "test");
			ResponseEntity<Void> response = restTemplate
					.withBasicAuth("test", "abc123")
					.postForEntity("/cashcard/", newCard, Void.class);

			log.info("Response from POST /cashcard/: " + response.toString());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
			assertThat(response.getBody()).isNull();

			//Test the Location header
			String location = response.getHeaders().getLocation().toString();
			log.info("Location header from POST /cashcard/: " + location);
			assertThat(location).isNotNull();
			ResponseEntity<String> getResponse = restTemplate
					.withBasicAuth("test", "abc123")
					.getForEntity(location, String.class);
			log.info("Response from GET " + location + ": " + getResponse.toString());
			assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(getResponse.getBody()).isEqualToIgnoringWhitespace("""
					{
					    "id": 5,
					    "cardNumber": "5555555555",
					    "balance": 500.00,
					    "owner": "test"
					}
					""");
		}
	}

	@Nested
	class getAllTests {
		@Test
		void shouldReturnAllCashCardsUnSorted() {
			ResponseEntity<String> response = restTemplate
					.withBasicAuth("test", "abc123")
					.getForEntity("/cashcard/", String.class);

			log.info("Response from /cashcard/: " + response.toString());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isEqualToIgnoringWhitespace("""
					[
					    {
					        "id": 1,
					        "cardNumber": "1111111111",
					        "balance": 100.00,
					        "owner": "test"
					    },
					    {
					        "id": 2,
					        "cardNumber": "2222222222",
					        "balance": 200.00,
					        "owner": "test"
					    },
					    {
					        "id": 3,
					        "cardNumber": "3333333333",
					        "balance": 300.00,
					        "owner": "test"
					    },
					    {
					        "id": 4,
					        "cardNumber": "4444444444",
					        "balance": 400.00,
					        "owner": "test"
					    }
					]
					""");
		}

		@Test
		void shouldReturnFirstPageOfCashCards() {
			ResponseEntity<String> response = restTemplate
					.withBasicAuth("test", "abc123")
					.getForEntity("/cashcard/?page=0&size=2", String.class);

			log.info("Response from /cashcard/?page=0&size=1: " + response.toString());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isEqualToIgnoringWhitespace("""
					[
					    {
					        "id": 1,
					        "cardNumber": "1111111111",
					        "balance": 100.00,
					        "owner": "test"
					    },
					    {
					        "id": 2,
					        "cardNumber": "2222222222",
					        "balance": 200.00,
					        "owner": "test"
					    }
					]
					""");
		}

		@Test
		void shouldReturnSecondPageOfCashCards() {
			ResponseEntity<String> response = restTemplate
					.withBasicAuth("test", "abc123")
					.getForEntity("/cashcard/?page=1&size=2", String.class);

			log.info("Response from /cashcard/?page=1&size=2: " + response.toString());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isEqualToIgnoringWhitespace("""
					[
					    {
					        "id": 3,
					        "cardNumber": "3333333333",
					        "balance": 300.00,
					        "owner": "test"
					    },
					    {
					        "id": 4,
					        "cardNumber": "4444444444",
					        "balance": 400.00,
					        "owner": "test"
					    }
					]
					""");
		}

		@Test
		void shouldReturnAllCashCardsSortedByBalance() {
			ResponseEntity<String> response = restTemplate
					.withBasicAuth("test", "abc123")
					.getForEntity("/cashcard/?page=0&size=4&sort=balance,desc", String.class);

			log.info("Response from /cashcard/?page=0&size=4&sort=balance,desc: " + response.toString());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isEqualToIgnoringWhitespace("""
					[
					    {
					        "id": 4,
					        "cardNumber": "4444444444",
					        "balance": 400.00,
					        "owner": "test"
					    },
					    {
					        "id": 3,
					        "cardNumber": "3333333333",
					        "balance": 300.00,
					        "owner": "test"
					    },
					    {
					        "id": 2,
					        "cardNumber": "2222222222",
					        "balance": 200.00,
					        "owner": "test"
					    },
					    {
					        "id": 1,
					        "cardNumber": "1111111111",
					        "balance": 100.00,
					        "owner": "test"
					    }
					]
					""");
		}

		@Test
		void returnAllCashCardsSortedByCardNumber() {
			ResponseEntity<String> response = restTemplate
					.withBasicAuth("test", "abc123")
					.getForEntity("/cashcard/?page=0&size=4&sort=cardNumber,asc", String.class);

			log.info("Response from /cashcard/?page=0&size=4&sort=cardNumber,asc: " + response.toString());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isEqualToIgnoringWhitespace("""
					[
					    {
					        "id": 1,
					        "cardNumber": "1111111111",
					        "balance": 100.00,
					        "owner": "test"
					    },
					    {
					        "id": 2,
					        "cardNumber": "2222222222",
					        "balance": 200.00,
					        "owner": "test"
					    },
					    {
					        "id": 3,
					        "cardNumber": "3333333333",
					        "balance": 300.00,
					        "owner": "test"
					    },
					    {
					        "id": 4,
					        "cardNumber": "4444444444",
					        "balance": 400.00,
					        "owner": "test"
					    }
					]
					""");
		}

	}


}
