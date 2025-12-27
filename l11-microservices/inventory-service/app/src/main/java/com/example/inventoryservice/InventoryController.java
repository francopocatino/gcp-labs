package com.example.inventoryservice;

import com.google.cloud.spring.data.firestore.FirestoreTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    private final FirestoreTemplate firestoreTemplate;

    public InventoryController(FirestoreTemplate firestoreTemplate) {
        this.firestoreTemplate = firestoreTemplate;
    }

    @PostMapping
    public ResponseEntity<?> addInventory(@RequestBody Map<String, Object> request) {
        try {
            String productId = (String) request.get("productId");
            int quantity = (Integer) request.get("quantity");

            Inventory inventory = new Inventory(productId, quantity);
            firestoreTemplate.save(inventory);

            logger.info("Added inventory for product: {} quantity: {}", productId, quantity);
            return ResponseEntity.ok(inventory);
        } catch (Exception e) {
            logger.error("Error adding inventory", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getInventory(@PathVariable String productId) {
        try {
            Inventory inventory = firestoreTemplate.findById(productId, Inventory.class);
            if (inventory == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(inventory);
        } catch (Exception e) {
            logger.error("Error getting inventory", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reserve")
    public ResponseEntity<?> reserveInventory(@RequestBody Map<String, Object> request) {
        try {
            String productId = (String) request.get("productId");
            int quantity = (Integer) request.get("quantity");

            Inventory inventory = firestoreTemplate.findById(productId, Inventory.class);
            if (inventory == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Product not found"));
            }

            if (inventory.getQuantity() < quantity) {
                return ResponseEntity.badRequest().body(Map.of("error", "Insufficient stock"));
            }

            inventory.setQuantity(inventory.getQuantity() - quantity);
            firestoreTemplate.save(inventory);

            logger.info("Reserved {} units of product: {}", quantity, productId);
            return ResponseEntity.ok(inventory);
        } catch (Exception e) {
            logger.error("Error reserving inventory", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
