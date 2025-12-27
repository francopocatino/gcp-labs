package com.example.userservice;

import com.google.cloud.spring.data.firestore.FirestoreTemplate;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final FirestoreTemplate firestoreTemplate;
    private final PubSubTemplate pubSubTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${pubsub.topic.name:user-events}")
    private String topicName;

    public UserController(FirestoreTemplate firestoreTemplate, PubSubTemplate pubSubTemplate) {
        this.firestoreTemplate = firestoreTemplate;
        this.pubSubTemplate = pubSubTemplate;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String name = request.get("name");

            // 1. Save user to Firestore
            User user = new User(email, name);
            User savedUser = firestoreTemplate.save(user);
            logger.info("User created: {} ({})", savedUser.getName(), savedUser.getId());

            // 2. Publish event to Pub/Sub
            Map<String, Object> event = new HashMap<>();
            event.put("userId", savedUser.getId());
            event.put("email", savedUser.getEmail());
            event.put("name", savedUser.getName());
            event.put("event", "user.created");
            event.put("timestamp", savedUser.getCreatedAt());

            String eventJson = objectMapper.writeValueAsString(event);
            pubSubTemplate.publish(topicName, eventJson);

            logger.info("Published user.created event for user: {}", savedUser.getId());

            return ResponseEntity.ok(savedUser);

        } catch (Exception e) {
            logger.error("Error creating user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable String userId) {
        try {
            User user = firestoreTemplate.findById(userId, User.class);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error getting user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
