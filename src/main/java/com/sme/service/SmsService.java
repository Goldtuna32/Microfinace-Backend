package com.sme.service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class SmsService {
    private final RestTemplate restTemplate;

    @Value("${ozeki.url:http://192.168.1.25:9509}")
    private String ozekiUrl;

    @Value("${ozeki.username:httpuser}")
    private String username;

    @Value("${ozeki.password:abc123}")
    private String password;

    @Autowired
    public SmsService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public void sendSms(String toPhoneNumber, String message) {
        try {
            String normalizedNumber = toPhoneNumber.startsWith("+") ? toPhoneNumber : "+95" + toPhoneNumber.replaceFirst("^0", "");
            String url = ozekiUrl + "/api/v1/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String jsonBody = String.format(
                    "{\"username\":\"%s\",\"password\":\"%s\",\"command\":\"sendmessage\",\"recipient\":\"%s\",\"messagedata\":\"%s\"}",
                    username, password, normalizedNumber, message
            );

            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("SMS sent to " + normalizedNumber + ". Response: " + response.getBody());
            } else {
                throw new RuntimeException("SMS failed with status: " + response.getStatusCode() + ", Response: " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Failed to send SMS to " + toPhoneNumber + ": " + e.getMessage());
            throw new RuntimeException("SMS sending failed", e);
        }
    }
}