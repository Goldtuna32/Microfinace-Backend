package com.sme.config;

import com.sme.security.JwtAuthEntryPoint;
import com.sme.security.JwtAuthenticationFilter;
import com.sme.service.impl.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;



@Configuration
@EnableWebSecurity
public class SecurityConfig {

      private final JwtAuthEntryPoint authEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;


    @Autowired
    public SecurityConfig(JwtAuthEntryPoint authEntryPoint, JwtAuthEntryPoint authEntryPoint1, JwtAuthenticationFilter jwtAuthenticationFilter, CustomUserDetailsService customUserDetailsService) {
        this.authEntryPoint = authEntryPoint1;
//        this.authEntryPoint = authEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService =customUserDetailsService;

    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http

                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(authEntryPoint)) // Handle Unauthorized
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login","/api/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/branches").hasAuthority("BRANCH_READ")
                        .requestMatchers(HttpMethod.GET, "/api/branches/{id}").hasAuthority("BRANCH_READ")
                        .requestMatchers(HttpMethod.GET, "/api/branches/paged").hasAuthority("BRANCH_READ")
                        .requestMatchers(HttpMethod.GET, "/api/branches/check-duplicate").hasAuthority("BRANCH_CREATE")
                        .requestMatchers(HttpMethod.POST, "/api/branches").hasAuthority("BRANCH_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/api/branches/{id}").hasAuthority("BRANCH_UPDATE")
                        .requestMatchers(HttpMethod.DELETE, "/api/branches/{id}").hasAuthority("BRANCH_DELETE")
                        .requestMatchers("/email-websocket").permitAll()
                        .requestMatchers("/api/email/list").permitAll()
                        .requestMatchers("/api/role-permissions").permitAll()
                        .anyRequest().authenticated()
                )
                .userDetailsService(customUserDetailsService)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }



    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}