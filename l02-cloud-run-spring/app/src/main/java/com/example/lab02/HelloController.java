package com.example.lab02;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HelloController {

  @GetMapping("/")
  public Map<String, Object> root() {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("service", "lab-02-cloud-run-spring");
    body.put("status", "ok");
    body.put("time", Instant.now().toString());
    return body;
  }

  @GetMapping("/health")
  public Map<String, Object> health() {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("healthy", true);
    return body;
  }
}

