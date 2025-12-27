package com.example.analyticsservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

@RestController
public class AnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);
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
                long timestamp = ((Number) event.get("timestamp")).longValue();

                trackSignup(userId, email, timestamp);

                logger.info("Tracked signup for user: {}", userId);
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            logger.error("Error processing message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void trackSignup(String userId, String email, long timestamp) {
        // Simulate analytics tracking (in production: send to BigQuery, Mixpanel, etc.)
        logger.info("=== ANALYTICS EVENT ===");
        logger.info("Event: user_signup");
        logger.info("User ID: {}", userId);
        logger.info("Email: {}", email);
        logger.info("Timestamp: {}", timestamp);
        logger.info("======================");
    }
}
