# Lab 10 - Observability

Making services production-ready with logging, monitoring, and error tracking.

## What Is Observability?

**The 3 Pillars:**
1. **Logs** - What happened (events, requests, errors)
2. **Metrics** - How much/how fast (request count, latency, CPU)
3. **Traces** - Path through system (distributed tracing)

**Why it matters:**
- Debug issues in production
- Know when things break
- Understand user behavior
- Optimize performance

---

## GCP Observability Tools

### Cloud Logging
Collects and stores logs from all services.

**Features:**
- Automatic collection from Cloud Run
- Structured JSON logs (searchable)
- Log-based metrics
- Export to BigQuery

### Cloud Monitoring
Metrics, dashboards, alerts.

**Built-in metrics:**
- Request count
- Latency (p50, p95, p99)
- Error rate
- Instance count

### Error Reporting
Automatically groups and tracks errors.

**Features:**
- Exception grouping
- Stack traces
- Notifications
- Trend analysis

---

## Part 1: Viewing Logs

### Cloud Run automatically logs:
- HTTP requests (method, path, status, latency)
- Container stdout/stderr
- Health checks

### View logs in Console:

1. Go to **Cloud Logging** → **Logs Explorer**
2. Filter by resource: `Cloud Run Revision → lab02-spring`
3. Common queries:

```
# All errors
severity >= ERROR

# Slow requests (>1s)
httpRequest.latency > "1s"

# Specific endpoint
httpRequest.requestUrl =~ "/items.*"

# Time range
timestamp >= "2024-12-26T00:00:00Z"
```

### View logs with gcloud:

```bash
# Recent logs from lab02
gcloud run services logs read lab02-spring --region us-central1 --limit 50

# Follow logs (tail -f)
gcloud run services logs tail lab02-spring --region us-central1

# Filter by severity
gcloud run services logs read lab02-spring --region us-central1 --log-filter="severity>=ERROR"
```

---

## Part 2: Structured Logging

**Problem with plain logs:**
```
System.out.println("User created item: " + name);
```
Output: `User created item: Laptop`

Hard to search, no metadata, not queryable.

**Better: Structured JSON logs**

**Add to any service (example: lab02):**

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class HelloController {
  private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

  @GetMapping("/")
  public Map<String, Object> root() {
    logger.info("Root endpoint accessed");

    // Structured log with fields
    logger.info("Request received",
      kv("method", "GET"),
      kv("path", "/"),
      kv("user_agent", request.getHeader("User-Agent"))
    );

    return Map.of("service", "lab02");
  }
}
```

**Output in Cloud Logging:**
```json
{
  "severity": "INFO",
  "message": "Request received",
  "method": "GET",
  "path": "/",
  "user_agent": "curl/7.68.0",
  "timestamp": "2024-12-26T15:30:00Z"
}
```

Now you can search: `jsonPayload.method="GET"`

---

## Part 3: Error Reporting

Cloud Run automatically sends errors to Error Reporting.

### How it works:

**Your code throws exception:**
```java
@GetMapping("/items/{id}")
public Item getItem(@PathVariable Long id) {
  return itemRepository.findById(id)
    .orElseThrow(() -> new ItemNotFoundException("Item not found: " + id));
}
```

**Cloud Run catches it:**
- Logs stack trace
- Sends to Error Reporting
- Groups similar errors
- Shows in Error Reporting dashboard

### View errors:

**Console:**
1. Go to **Error Reporting**
2. See grouped errors with count
3. Click for stack traces and occurrences

**gcloud:**
```bash
gcloud error-reporting events list --service lab02-spring
```

### Custom error logging:

```java
try {
  riskyOperation();
} catch (Exception e) {
  logger.error("Failed to process item",
    kv("item_id", itemId),
    kv("error", e.getMessage()),
    e  // Include stack trace
  );
  throw e;
}
```

---

## Part 4: Metrics & Dashboards

### Built-in Cloud Run metrics:

**View in Console:**
1. Go to **Cloud Run → your-service → Metrics**
2. See graphs for:
   - Request count
   - Request latency
   - Container instance count
   - CPU/Memory usage

**Query with gcloud:**
```bash
# Request count (last hour)
gcloud monitoring time-series list \
  --filter='metric.type="run.googleapis.com/request_count"' \
  --start-time=$(date -u -d '1 hour ago' +%Y-%m-%dT%H:%M:%SZ)
```

### Custom metrics:

**Example: Track database queries**

```java
import io.opencensus.stats.*;

// Define metric
private static final MeasureLong DB_QUERIES =
  MeasureLong.create("db/queries", "Database queries", "1");

// Record metric
@GetMapping("/items")
public List<Item> getItems() {
  statsRecorder.newMeasureMap()
    .put(DB_QUERIES, 1)
    .record();

  return itemRepository.findAll();
}
```

---

## Part 5: Practical Exercises

### Exercise 1: Find Slow Requests
```bash
# Deploy lab02
# Make some requests
curl https://your-service.run.app/

# Find requests >100ms
# Go to Logs Explorer:
httpRequest.latency > "0.1s"

# See which endpoints are slow
```

### Exercise 2: Trigger and Find an Error
```bash
# Cause an error (request missing parameter)
curl https://your-service.run.app/items/999

# Go to Error Reporting
# See grouped error with stack trace
```

### Exercise 3: Create Alert
1. Go to **Cloud Monitoring → Alerting**
2. Create policy:
   - Condition: Error rate > 5%
   - Notification: Email
3. Trigger it by causing errors
4. Receive alert email

### Exercise 4: Build Dashboard
1. Go to **Cloud Monitoring → Dashboards**
2. Create dashboard with:
   - Request count (line chart)
   - Error rate (gauge)
   - P95 latency (line chart)
   - Active instances (stacked area)
3. Save and share

---

## Best Practices

### DO:
- ✅ Use structured logging (JSON)
- ✅ Include request ID in all logs (for tracing)
- ✅ Log at appropriate levels (DEBUG, INFO, WARN, ERROR)
- ✅ Include context (user_id, item_id, etc.)
- ✅ Set up alerts for critical errors

### DON'T:
- ❌ Log sensitive data (passwords, tokens)
- ❌ Log on every request (too noisy)
- ❌ Use `System.out.println` in production
- ❌ Ignore error patterns
- ❌ Over-log (expensive, hard to find signal)

---

## Log Levels Guide

```java
logger.debug("Detailed info for debugging");    // Dev only
logger.info("Normal operation");                 // Key events
logger.warn("Something unexpected but handled"); // Review later
logger.error("Something failed", exception);     // Needs attention
```

**In production:** Set level to INFO (hide DEBUG logs)

---

## Costs

**Cloud Logging:**
- First 50GB/month: Free
- After: $0.50/GB

**Cloud Monitoring:**
- Free tier covers most small apps
- Custom metrics: First 150 metrics free

**Error Reporting:**
- Free

**Your usage:** Probably $0-5/month

---

## Real-World Scenarios

### Scenario 1: "API is slow"
1. Check Cloud Monitoring → P95 latency
2. Filter logs: `httpRequest.latency > "1s"`
3. Find slow endpoint
4. Add detailed logging to that endpoint
5. Optimize query/logic

### Scenario 2: "Users reporting errors"
1. Check Error Reporting
2. See stack trace and frequency
3. Filter logs by error message
4. Find root cause
5. Deploy fix
6. Verify error rate drops

### Scenario 3: "Unexpected costs"
1. Check Cloud Monitoring → Instance count
2. See if autoscaling too aggressive
3. Review logs for traffic patterns
4. Adjust concurrency settings
5. Monitor cost reduction

---

## Quick Reference

### View service logs
```bash
gcloud run services logs read SERVICE_NAME --region REGION --limit 50
```

### View errors
```bash
gcloud error-reporting events list --service SERVICE_NAME
```

### Export logs to BigQuery
```bash
gcloud logging sinks create my-sink \
  bigquery.googleapis.com/projects/PROJECT/datasets/logs \
  --log-filter='resource.type="cloud_run_revision"'
```

### Create alert
```bash
gcloud alpha monitoring policies create \
  --notification-channels=CHANNEL_ID \
  --display-name="High error rate" \
  --condition-threshold-value=0.05 \
  --condition-threshold-filter='metric.type="run.googleapis.com/request_count" AND metric.label.response_code_class="5xx"'
```

---

## What You Learned

- How Cloud Logging, Monitoring, and Error Reporting work
- Viewing and filtering logs
- Structured vs unstructured logging
- Error tracking and debugging
- Metrics and dashboards
- Production debugging workflows

## Practice

Try these on your deployed services:
1. View logs for lab02 in Cloud Logging
2. Trigger an error and find it in Error Reporting
3. Create a simple dashboard for lab09 (database service)
4. Set up an alert for error rate > 5%
5. Find your slowest endpoint using log filters

---

**No new code needed** - all your services already send logs/metrics to GCP automatically!
