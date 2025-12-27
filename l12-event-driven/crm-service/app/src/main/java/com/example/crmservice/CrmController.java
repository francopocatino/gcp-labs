package com.example.crmservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

@RestController
public class CrmController {

    private static final Logger logger = LoggerFactory.getLogger(CrmController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/")
    public ResponseEntity<?> handlePubSubPush(@RequestBody Map<String, Object> body) {
        try {
            Map<String, Object> message = (Map<String, Object>) body.get("message");
            if (message == null) {
                return ResponseEntity.badRequest().build();
            }

            String data = (String) message.get("data");
            byte[] decodedBytes = Base64.getDecoder().decode(data);
            String decodedData = new String(decodedBytes);

            Map<String, Object> event = objectMapper.readValue(decodedData, Map.class);

            String eventType = (String) event.get("event");
            if ("user.created".equals(eventType)) {
                String userId = (String) event.get("userId");
                String email = (String) event.get("email");
                String name = (String) event.get("name");

                createCrmContact(userId, email, name);

                logger.info("Created CRM contact for: {}", email);
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            logger.error("Error processing message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void createCrmContact(String userId, String email, String name) {
        // Simulate CRM integration (in production: Salesforce, HubSpot API, etc.)
        logger.info("=== CRM INTEGRATION ===");
        logger.info("Action: Create Contact");
        logger.info("Name: {}", name);
        logger.info("Email: {}", email);
        logger.info("User ID: {}", userId);
        logger.info("======================");
    }
}
