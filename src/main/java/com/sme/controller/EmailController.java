package com.sme.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sme.service.EmailService;
import com.sme.service.EmailService.EmailUpdate;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public String sendEmail(@RequestParam String to,
                            @RequestParam String subject,
                            @RequestParam String body) {
        try {
            emailService.sendEmail(to, subject, body);
            return "Email sent successfully";
        } catch (MessagingException | JsonProcessingException e) {
            return "Failed to send email: " + e.getMessage();
        }
    }

    @GetMapping("/list")
    public List<EmailUpdate> getEmailList() throws Exception {
        return emailService.fetchEmailsFromAccount();
    }
}