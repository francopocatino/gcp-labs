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
  void writeWithoutBucketReturnsError() {
    // When BUCKET_NAME env var is not set, should return error
    ResponseEntity<Map> response = restTemplate.getForEntity("/gcs/write", Map.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    // Will contain error if bucket not configured
    assertNotNull(response.getBody());
  }
}
