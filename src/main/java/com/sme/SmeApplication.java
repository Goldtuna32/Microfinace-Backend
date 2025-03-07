package com.sme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Enable scheduling for the application
public class SmeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmeApplication.class, args);
    }
}
