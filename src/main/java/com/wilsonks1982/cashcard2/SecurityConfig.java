/***
 * Copyright (c) 2024, Wilson K. Sam All rights reserved.Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is free for use and redistribution, but not for commercial purposes.
 *
 *
 *
 */
package com.wilsonks1982.cashcard2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@PropertySource("classpath:security-config.properties")
public class SecurityConfig {

    @Value("${app.security.admin.role}")
    private String adminRole;
    @Value("${app.security.user.role}")
    private String userRole;

    @Value("${app.security.admin.username}")
    private String adminUsername;

    @Value("${app.security.admin.password}")
    private String adminPassword;


    @Value("${app.security.user1.username}")
    private String user1Username;

    @Value("${app.security.user1.password}")
    private String user1Password;

    @Value("${app.security.user2.username}")
    private String user2Username;

    @Value("${app.security.user2.password}")
    private String user2Password;


    @Bean
    SecurityFilterChain customSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(
                    request -> request.requestMatchers("/h2-console/**").permitAll()
                                      .requestMatchers(HttpMethod.GET, "/cashcard/**").hasAnyRole(adminRole, userRole)
                                      .requestMatchers(HttpMethod.POST,"/cashcard/**").hasAnyRole(adminRole, userRole)
                                      .requestMatchers(HttpMethod.PUT,"/cashcard/**").hasAnyRole(adminRole, userRole)
                                      .requestMatchers(HttpMethod.DELETE,"/cashcard/**").hasAnyRole(adminRole, userRole)
                                      .anyRequest().permitAll()//Allow all other requests without authentication
                )
                .httpBasic(Customizer.withDefaults())//HTTP Basic sends credentials in Base64 (easily decoded)
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        User.UserBuilder userBuilder = User.builder().passwordEncoder(passwordEncoder::encode);
        UserDetails admin = userBuilder
                .username(adminUsername)
                .password(adminPassword)
                .roles(adminRole)
                .build();

        UserDetails user1 = userBuilder
                .username(user1Username)
                .password(user1Password)
                .roles(userRole)
                .build();

        UserDetails user2 = userBuilder
                .username(user2Username)
                .password(user2Password)
                .roles(userRole)
                .build();


        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(admin);
        manager.createUser(user1);
        manager.createUser(user2);
        return manager;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
