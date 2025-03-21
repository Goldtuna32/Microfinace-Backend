package com.sme.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private EmailWebSocketHandler emailWebSocketHandler;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${spring.mail.password}")
    private String senderPassword;

    @Value("${spring.mail.imap.host}")
    private String imapHost;

    @Value("${spring.mail.imap.port}")
    private String imapPort;

    @Value("${spring.mail.imap.username}")
    private String imapUsername;

    @Value("${spring.mail.imap.password}")
    private String imapPassword;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendEmail(String to, String subject, String body) throws MessagingException, JsonProcessingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(senderEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        javaMailSender.send(message);
        System.out.println("Email sent successfully to: " + to);

        EmailUpdate update = new EmailUpdate(to, subject, body, System.currentTimeMillis(), "SENT");
        String jsonUpdate = objectMapper.writeValueAsString(update);
        emailWebSocketHandler.sendEmailUpdate(jsonUpdate);
    }

    public List<EmailUpdate> fetchEmailsFromAccount() throws MessagingException {
        List<EmailUpdate> emailList = new ArrayList<>();
        Properties props = new Properties();
        props.put("mail.imaps.host", imapHost);
        props.put("mail.imaps.port", imapPort);
        props.put("mail.imaps.ssl.enable", "true");
        props.put("mail.store.protocol", "imaps");

        Session session = Session.getInstance(props, null);
        session.setDebug(true);
        Store store = session.getStore("imaps");
        store.connect(imapHost, imapUsername, imapPassword);

        // Fetch from INBOX (received emails)
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
        fetchEmailsFromFolder(inbox, emailList);
        inbox.close(false);

        // Fetch from [Gmail]/Sent Mail (sent emails)
        Folder sentFolder = store.getFolder("[Gmail]/Sent Mail");
        sentFolder.open(Folder.READ_ONLY);
        fetchEmailsFromFolder(sentFolder, emailList);
        sentFolder.close(false);

        store.close();
        return emailList;
    }

    private void fetchEmailsFromFolder(Folder folder, List<EmailUpdate> emailList) throws MessagingException {
        Message[] messages = folder.getMessages();
        System.out.println("Found " + messages.length + " messages in " + folder.getFullName());
        for (Message msg : messages) {
            String to = msg.getRecipients(Message.RecipientType.TO) != null && msg.getRecipients(Message.RecipientType.TO).length > 0
                    ? String.join(",", msg.getRecipients(Message.RecipientType.TO)[0].toString())
                    : "Unknown";
            String subject = msg.getSubject() != null ? msg.getSubject() : "(No Subject)";
            String body;
            try {
                body = getTextFromMessage(msg);
            } catch (Exception e) {
                body = "Error reading body: " + e.getMessage();
            }
            long timestamp = msg.getSentDate() != null ? msg.getSentDate().getTime() : System.currentTimeMillis();
            String direction = msg.getFrom() != null && msg.getFrom()[0].toString().contains(senderEmail) ? "SENT" : "RECEIVED";

            EmailUpdate email = new EmailUpdate(to, subject, body, timestamp, direction);
            emailList.add(email);
        }
    }

    private String getTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    return bodyPart.getContent().toString();
                }
            }
        }
        return "";
    }

    public static class EmailUpdate {
        private String to;
        private String subject;
        private String body;
        private long timestamp;
        private String direction;

        public EmailUpdate(String to, String subject, String body, long timestamp, String direction) {
            this.to = to;
            this.subject = subject;
            this.body = body;
            this.timestamp = timestamp;
            this.direction = direction;
        }

        public String getTo() { return to; }
        public String getSubject() { return subject; }
        public String getBody() { return body; }
        public long getTimestamp() { return timestamp; }
        public String getDirection() { return direction; }
    }
}