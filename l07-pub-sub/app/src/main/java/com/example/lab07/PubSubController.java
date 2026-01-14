package com.example.lab07;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@RestController
public class PubSubController {

  private final String projectId = System.getenv().getOrDefault("PROJECT_ID", "");
  private final String topicId = System.getenv().getOrDefault("PUBSUB_TOPIC", "lab07-topic");

  @GetMapping("/")
  public Map<String, Object> root() {
    return Map.of(
        "service", "lab07-pubsub",
        "project", projectId,
        "topic", topicId,
        "endpoints", Map.of(
            "publish", "POST /pubsub/publish with {\"message\":\"text\"}",
            "consume", "POST /pubsub/consume (push subscription endpoint)"
        )
    );
  }

  @GetMapping("/health")
  public Map<String, Object> health() {
    return Map.of("healthy", true);
  }

  @PostMapping("/pubsub/publish")
  public Map<String, Object> publish(@RequestBody Map<String, String> body) {

    if (projectId.isEmpty()) {
      return Map.of("error", "PROJECT_ID env var not set");
    }

    String payload = body.getOrDefault("message", "hello pubsub");
    TopicName topicName = TopicName.of(projectId, topicId);

    Publisher publisher = null;
    try {
      publisher = Publisher.newBuilder(topicName).build();

      PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
          .setData(ByteString.copyFrom(payload, StandardCharsets.UTF_8))
          .build();

      // publish() is async, get() blocks until complete
      String messageId = publisher.publish(pubsubMessage).get();

      return Map.of(
          "published", true,
          "messageId", messageId,
          "topic", topicName.toString(),
          "payload", payload
      );

    } catch (Exception e) {
      return Map.of(
          "error", "Failed to publish: " + e.getMessage()
      );

    } finally {
      if (publisher != null) {
        publisher.shutdown();
      }
    }
  }

  // Push subscription endpoint - Pub/Sub calls this
  @PostMapping("/pubsub/consume")
  public ResponseEntity<String> consume(@RequestBody Map<String, Object> payload) {

    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> message = (Map<String, Object>) payload.get("message");

      if (message == null) {
        return ResponseEntity.badRequest().body("Invalid payload: missing 'message'");
      }

      String dataBase64 = (String) message.get("data");
      if (dataBase64 == null) {
        return ResponseEntity.badRequest().body("Invalid payload: missing 'data'");
      }

      String decoded = new String(
          Base64.getDecoder().decode(dataBase64),
          StandardCharsets.UTF_8
      );

      System.out.println("Received Pub/Sub message: " + decoded);

      // HTTP 2xx = ACK (Pub/Sub won't retry)
      // HTTP != 2xx = NACK (Pub/Sub will retry)
      return ResponseEntity.ok("ACK");

    } catch (Exception e) {
      System.err.println("Failed to process message: " + e.getMessage());
      // Return 500 to trigger Pub/Sub retry (at-least-once delivery)
      return ResponseEntity.internalServerError().body("NACK");
    }
  }
}
