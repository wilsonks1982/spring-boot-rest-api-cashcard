package com.wilsonks1982.cashcard2;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTests {


	@Autowired
	ApplicationContext context;

	@Nested
	class beanTests {
		@Test
		void contextLoads() {
			assertThat(context).isNotNull();
		}

		@Test
		void shouldNotHaveDefaultSecurityFilterChainBean() {
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



}
