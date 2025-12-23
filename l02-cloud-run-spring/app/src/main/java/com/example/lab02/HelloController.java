package com.example.lab02;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HelloController {

  private final String appName = System.getenv().getOrDefault("APP_NAME", "undefined");
  private final String message = System.getenv().getOrDefault("MESSAGE", "no-message");
  private final String profile = System.getenv().getOrDefault("SPRING_PROFILES_ACTIVE", "default");

  // GCS
  private final Storage storage = StorageOptions.getDefaultInstance().getService();
  private final String bucketName = System.getenv().getOrDefault("BUCKET_NAME", "");

  // Pub/Sub
  private final String projectId = System.getenv().getOrDefault("PROJECT_ID", "");
  private final String topicId = System.getenv().getOrDefault("PUBSUB_TOPIC", "lab07-topic");

  /* ---------------- BASIC ---------------- */

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

  /* ---------------- GCS ---------------- */

  @GetMapping("/gcs/write")
  public Map<String, Object> write() {
    String objectName = "hello.txt";
    String content = "Hello from Cloud Run at " + Instant.now();

    BlobId blobId = BlobId.of(bucketName, objectName);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
        .setContentType("text/plain")
        .build();

    storage.create(blobInfo, content.getBytes(StandardCharsets.UTF_8));

    return Map.of(
        "bucket", bucketName,
        "object", objectName,
        "written", true
    );
  }

  @GetMapping("/gcs/read")
  public Map<String, Object> read() {
    String objectName = "hello.txt";

    Blob blob = storage.get(BlobId.of(bucketName, objectName));
    String content = blob == null
        ? "NOT_FOUND"
        : new String(blob.getContent(), StandardCharsets.UTF_8);

    return Map.of(
        "bucket", bucketName,
        "object", objectName,
        "content", content
    );
  }

  /* ---------------- PUB/SUB PRODUCER ---------------- */

  @PostMapping("/pubsub/publish")
  public Map<String, Object> publish(@RequestBody Map<String, String> body) throws Exception {

    if (projectId.isBlank()) {
      throw new IllegalStateException("PROJECT_ID env var is missing");
    }

    String payload = body.getOrDefault("message", "hello pubsub");
    TopicName topicName = TopicName.of(projectId, topicId);

    Publisher publisher = Publisher.newBuilder(topicName).build();
    try {
      PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
          .setData(ByteString.copyFrom(payload, StandardCharsets.UTF_8))
          .build();

      String messageId = publisher.publish(pubsubMessage).get();

      return Map.of(
          "published", true,
          "messageId", messageId,
          "topic", topicName.toString()
      );
    } finally {
      publisher.shutdown();
    }
  }

  /* ---------------- PUB/SUB CONSUMER ---------------- */

  @PostMapping("/pubsub/consume")
  public ResponseEntity<String> consume(@RequestBody Map<String, Object> payload) {
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> message = (Map<String, Object>) payload.get("message");

      String dataBase64 = (String) message.get("data");
      String decoded = new String(
          Base64.getDecoder().decode(dataBase64),
          StandardCharsets.UTF_8
      );

      System.out.println("Received event: " + decoded);

      // HTTP 2xx = ACK
      return ResponseEntity.ok("ACK");
    } catch (Exception e) {
      // HTTP != 2xx => Pub/Sub retry (at-least-once)
      System.err.println("Failed to process Pub/Sub message: " + e.getMessage());
      return ResponseEntity.internalServerError().body("NACK");
    }
  }
}
