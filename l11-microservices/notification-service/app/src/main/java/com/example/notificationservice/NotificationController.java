package com.example.notificationservice;

import com.google.cloud.spring.data.firestore.FirestoreTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final FirestoreTemplate firestoreTemplate;

    public NotificationController(FirestoreTemplate firestoreTemplate) {
        this.firestoreTemplate = firestoreTemplate;
    }

    @PostMapping("/order")
    public ResponseEntity<?> sendOrderNotification(@RequestBody Map<String, Object> request) {
        try {
            String orderId = (String) request.get("orderId");
            String customerId = (String) request.get("customerId");
            String productId = (String) request.get("productId");
            int quantity = (Integer) request.get("quantity");

            String message = String.format(
                    "Order %s confirmed! Product: %s, Quantity: %d",
                    orderId, productId, quantity
            );

            Notification notification = new Notification(orderId, customerId, message);
            firestoreTemplate.save(notification);

            logger.info("Notification sent to customer: {} for order: {}", customerId, orderId);
            logger.info("Message: {}", message);

            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            logger.error("Error sending notification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
