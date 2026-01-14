package com.example.lab02;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HelloController {

  // Read from environment variables (can be updated without rebuilding)
  private final String appName = System.getenv().getOrDefault("APP_NAME", "undefined");
  private final String message = System.getenv().getOrDefault("MESSAGE", "no-message");
  private final String profile = System.getenv().getOrDefault("SPRING_PROFILES_ACTIVE", "default");

  @GetMapping("/")
  public Map<String, Object> root() {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("service", appName);
    body.put("message", message);
    body.put("profile", profile);
    body.put("time", Instant.now().toString());
    return body;
  }

  @GetMapping("/health")
  public Map<String, Object> health() {
    return Map.of("healthy", true);
  }
}
