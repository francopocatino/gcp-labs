package com.example.lab02;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloControllerTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void rootEndpointReturnsOk() {
    ResponseEntity<Map> response = restTemplate.getForEntity("/", Map.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().containsKey("service"));
  }

  @Test
  void healthEndpointReturnsHealthy() {
    ResponseEntity<Map> response = restTemplate.getForEntity("/health", Map.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(true, response.getBody().get("healthy"));
  }
}
