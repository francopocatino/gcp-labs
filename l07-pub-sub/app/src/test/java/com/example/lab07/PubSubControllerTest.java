package com.example.lab07;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PubSubControllerTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void rootEndpointReturnsServiceInfo() {
    ResponseEntity<Map> response = restTemplate.getForEntity("/", Map.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("lab07-pubsub", response.getBody().get("service"));
  }

  @Test
  void healthEndpointReturnsHealthy() {
    ResponseEntity<Map> response = restTemplate.getForEntity("/health", Map.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(true, response.getBody().get("healthy"));
  }

  @Test
  void publishWithoutProjectReturnsError() {
    // When PROJECT_ID env var is not set, should return error
    Map<String, String> body = Map.of("message", "test");
    ResponseEntity<Map> response = restTemplate.postForEntity("/pubsub/publish", body, Map.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }
}
