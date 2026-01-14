package com.example.lab09;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
class ItemControllerTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void rootEndpointReturnsServiceInfo() {
    ResponseEntity<Map> response = restTemplate.getForEntity("/", Map.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("lab09-cloud-sql", response.getBody().get("service"));
  }

  @Test
  void healthEndpointReturnsHealthy() {
    ResponseEntity<Map> response = restTemplate.getForEntity("/health", Map.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(true, response.getBody().get("healthy"));
  }

  @Test
  void createAndRetrieveItem() {
    // Create an item
    Item newItem = new Item("Test Item", "Test Description");
    ResponseEntity<Item> createResponse = restTemplate.postForEntity("/items", newItem, Item.class);

    assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
    assertNotNull(createResponse.getBody());
    assertNotNull(createResponse.getBody().getId());
    assertEquals("Test Item", createResponse.getBody().getName());

    // Retrieve all items
    ResponseEntity<Item[]> getAllResponse = restTemplate.getForEntity("/items", Item[].class);
    assertEquals(HttpStatus.OK, getAllResponse.getStatusCode());
    assertTrue(getAllResponse.getBody().length > 0);
  }
}
