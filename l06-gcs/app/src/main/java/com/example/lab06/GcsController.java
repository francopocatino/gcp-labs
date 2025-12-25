package com.example.lab06;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

@RestController
public class GcsController {

  // Cloud Storage client is thread-safe and should be reused
  // Uses Application Default Credentials (service account when running on Cloud Run)
  private final Storage storage = StorageOptions.getDefaultInstance().getService();

  private final String bucketName = System.getenv().getOrDefault("BUCKET_NAME", "");

  @GetMapping("/")
  public Map<String, Object> root() {
    return Map.of(
        "service", "lab06-gcs",
        "bucket", bucketName,
        "endpoints", Map.of(
            "write", "GET /gcs/write?content=...",
            "read", "GET /gcs/read?name=...",
            "list", "GET /gcs/list"
        )
    );
  }

  @GetMapping("/health")
  public Map<String, Object> health() {
    return Map.of("healthy", true);
  }

  @GetMapping("/gcs/write")
  public Map<String, Object> write(@RequestParam(defaultValue = "hello.txt") String name,
                                     @RequestParam(defaultValue = "") String content) {

    if (bucketName.isEmpty()) {
      return Map.of("error", "BUCKET_NAME env var not set");
    }

    String fileContent = content.isEmpty()
        ? "Hello from Cloud Run at " + Instant.now()
        : content;

    BlobId blobId = BlobId.of(bucketName, name);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
        .setContentType("text/plain")
        .build();

    storage.create(blobInfo, fileContent.getBytes(StandardCharsets.UTF_8));

    return Map.of(
        "bucket", bucketName,
        "object", name,
        "written", true,
        "size", fileContent.length()
    );
  }

  @GetMapping("/gcs/read")
  public Map<String, Object> read(@RequestParam(defaultValue = "hello.txt") String name) {

    if (bucketName.isEmpty()) {
      return Map.of("error", "BUCKET_NAME env var not set");
    }

    Blob blob = storage.get(BlobId.of(bucketName, name));

    if (blob == null) {
      return Map.of(
          "bucket", bucketName,
          "object", name,
          "found", false
      );
    }

    String content = new String(blob.getContent(), StandardCharsets.UTF_8);

    return Map.of(
        "bucket", bucketName,
        "object", name,
        "found", true,
        "content", content,
        "size", blob.getSize()
    );
  }

  @GetMapping("/gcs/list")
  public Map<String, Object> list() {

    if (bucketName.isEmpty()) {
      return Map.of("error", "BUCKET_NAME env var not set");
    }

    var objects = storage.list(bucketName).streamAll()
        .map(blob -> Map.of(
            "name", blob.getName(),
            "size", blob.getSize(),
            "updated", blob.getUpdateTimeOffsetDateTime().toString()
        ))
        .toList();

    return Map.of(
        "bucket", bucketName,
        "count", objects.size(),
        "objects", objects
    );
  }
}
