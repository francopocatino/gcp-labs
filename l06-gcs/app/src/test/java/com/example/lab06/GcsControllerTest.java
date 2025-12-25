package com.example.lab06;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GcsControllerTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void rootEndpointReturnsServiceInfo() {
    ResponseEntity<Map> response = restTemplate.getForEntity("/", Map.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("lab06-gcs", response.getBody().get("service"));
  }

  @Test
  void healthEndpointReturnsHealthy() {
    ResponseEntity<Map> response = restTemplate.getForEntity("/health", Map.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(true, response.getBody().get("healthy"));
  }

  @Test
  void listEndpointResponds() {
    // Simple test - just verify endpoint responds (doesn't actually call GCS)
    ResponseEntity<Map> response = restTemplate.getForEntity("/gcs/list", Map.class);
    // Will return 200 with list or error message depending on bucket config
    assertNotNull(response.getBody());
  }
}
