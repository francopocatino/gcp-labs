package com.example.orderservice;

import com.google.cloud.spring.data.firestore.FirestoreTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final FirestoreTemplate firestoreTemplate;
    private final RestTemplate restTemplate;

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    @Value("${notification.service.url}")
    private String notificationServiceUrl;

    public OrderController(FirestoreTemplate firestoreTemplate, RestTemplate restTemplate) {
        this.firestoreTemplate = firestoreTemplate;
        this.restTemplate = restTemplate;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> request) {
        try {
            String customerId = (String) request.get("customerId");
            String productId = (String) request.get("productId");
            int quantity = (Integer) request.get("quantity");

            // 1. Check inventory
            logger.info("Checking inventory for product: {}", productId);
            boolean inventoryAvailable = checkInventory(productId, quantity);
            if (!inventoryAvailable) {
                return ResponseEntity.badRequest().body(Map.of("error", "Insufficient stock"));
            }

            // 2. Reserve inventory
            logger.info("Reserving {} units of product: {}", quantity, productId);
            boolean reserved = reserveInventory(productId, quantity);
            if (!reserved) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed to reserve inventory"));
            }

            // 3. Create order
            Order order = new Order(customerId, productId, quantity, "CONFIRMED");
            Order savedOrder = firestoreTemplate.save(order);
            logger.info("Order created: {}", savedOrder.getId());

            // 4. Send notification (async, don't fail if this fails)
            try {
                sendNotification(savedOrder);
            } catch (Exception e) {
                logger.error("Failed to send notification, but order is created", e);
            }

            return ResponseEntity.ok(savedOrder);

        } catch (Exception e) {
            logger.error("Error creating order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable String orderId) {
        try {
            Order order = firestoreTemplate.findById(orderId, Order.class);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            logger.error("Error getting order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private boolean checkInventory(String productId, int quantity) {
        try {
            String url = inventoryServiceUrl + "/inventory/" + productId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                int available = (Integer) response.getBody().get("quantity");
                return available >= quantity;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error checking inventory", e);
            return false;
        }
    }

    private boolean reserveInventory(String productId, int quantity) {
        try {
            String url = inventoryServiceUrl + "/inventory/reserve";
            Map<String, Object> request = new HashMap<>();
            request.put("productId", productId);
            request.put("quantity", quantity);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            logger.error("Error reserving inventory", e);
            return false;
        }
    }

    private void sendNotification(Order order) {
        try {
            String url = notificationServiceUrl + "/notifications/order";
            Map<String, Object> request = new HashMap<>();
            request.put("orderId", order.getId());
            request.put("customerId", order.getCustomerId());
            request.put("productId", order.getProductId());
            request.put("quantity", order.getQuantity());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            restTemplate.postForEntity(url, entity, Map.class);
            logger.info("Notification sent for order: {}", order.getId());
        } catch (Exception e) {
            logger.error("Error sending notification", e);
            throw e;
        }
    }
}
