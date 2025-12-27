package com.example.emailservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

@RestController
public class EmailController {

    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/")
    public ResponseEntity<?> handlePubSubPush(@RequestBody Map<String, Object> body) {
        try {
            // Pub/Sub push sends message in this format:
            // { "message": { "data": "base64-encoded-json", "messageId": "..." } }

            Map<String, Object> message = (Map<String, Object>) body.get("message");
            if (message == null) {
                logger.warn("No message in request body");
                return ResponseEntity.badRequest().build();
            }

            String data = (String) message.get("data");
            if (data == null) {
                logger.warn("No data in message");
                return ResponseEntity.badRequest().build();
            }

            // Decode base64
            byte[] decodedBytes = Base64.getDecoder().decode(data);
            String decodedData = new String(decodedBytes);

            // Parse JSON event
            Map<String, Object> event = objectMapper.readValue(decodedData, Map.class);

            String eventType = (String) event.get("event");
            if ("user.created".equals(eventType)) {
                String userId = (String) event.get("userId");
                String email = (String) event.get("email");
                String name = (String) event.get("name");

                sendWelcomeEmail(userId, email, name);

                logger.info("Sent welcome email to: {} ({})", name, email);
            }

            // Return 200 to ack the message
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            logger.error("Error processing message", e);
            // Return 4xx to avoid retry, 5xx to retry
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void sendWelcomeEmail(String userId, String email, String name) {
        // Simulate sending email (in production: use SendGrid, Mailgun, etc.)
        logger.info("=== SENDING EMAIL ===");
        logger.info("To: {}", email);
        logger.info("Subject: Welcome to our platform!");
        logger.info("Body: Hello {}, welcome! Your user ID is: {}", name, userId);
        logger.info("====================");
    }
}
