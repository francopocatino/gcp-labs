package com.example.lab09;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class ItemController {

  @Autowired
  private ItemRepository itemRepository;

  @GetMapping("/")
  public Map<String, Object> root() {
    long count = itemRepository.count();
    return Map.of(
      "service", "lab09-cloud-sql",
      "description", "Cloud Run + Cloud SQL PostgreSQL",
      "items_in_db", count
    );
  }

  @GetMapping("/health")
  public ResponseEntity<Map<String, Object>> health() {
    try {
      // Check database connectivity
      long count = itemRepository.count();
      return ResponseEntity.ok(Map.of(
        "status", "UP",
        "database", "connected",
        "items_count", count
      ));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
        "status", "DOWN",
        "database", "disconnected",
        "error", e.getMessage()
      ));
    }
  }

  // CREATE
  @PostMapping("/items")
  public ResponseEntity<Item> createItem(@RequestBody Item item) {
    Item saved = itemRepository.save(item);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  // READ ALL
  @GetMapping("/items")
  public List<Item> getAllItems() {
    return itemRepository.findAll();
  }

  // READ ONE
  @GetMapping("/items/{id}")
  public ResponseEntity<Item> getItem(@PathVariable Long id) {
    return itemRepository.findById(id)
      .map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }

  // UPDATE
  @PutMapping("/items/{id}")
  public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item updatedItem) {
    return itemRepository.findById(id)
      .map(item -> {
        item.setName(updatedItem.getName());
        item.setDescription(updatedItem.getDescription());
        Item saved = itemRepository.save(item);
        return ResponseEntity.ok(saved);
      })
      .orElse(ResponseEntity.notFound().build());
  }

  // DELETE
  @DeleteMapping("/items/{id}")
  public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
    if (itemRepository.existsById(id)) {
      itemRepository.deleteById(id);
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }
}
