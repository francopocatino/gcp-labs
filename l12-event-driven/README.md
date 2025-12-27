# Lab 12 - Event-Driven Architecture

Async communication via events. Services react to events, don't call each other directly.

## Architecture

```
User Signup
    ↓
User Service → Pub/Sub (user.created event)
                  ├─→ Email Service (send welcome email)
                  ├─→ Analytics Service (track signup)
                  └─→ CRM Service (create contact)
```

**Pattern:**
- Event producer publishes to topic
- Multiple subscribers listen
- Services don't know about each other
- At-least-once delivery

**Stack:**
- 4 Cloud Run services (producer + 3 consumers)
- Pub/Sub topic (event bus)
- Push subscriptions
- Firestore (optional state tracking)

## Event-Driven vs Microservices

| Aspect | Microservices (Lab 11) | Event-Driven (Lab 12) |
|--------|----------------------|---------------------|
| Communication | Sync HTTP | Async Pub/Sub |
| Coupling | Tight (caller knows callee) | Loose (don't know consumers) |
| Failure | Caller handles error | Retry automatically |
| Scaling | Both services must be up | Independent scaling |
| Latency | Immediate response | Eventual consistency |

## Use Cases

**When to use:**
- Need to decouple services
- One event → multiple reactions
- Don't need immediate response
- Async workflows (signup, orders, notifications)
- High scalability requirements

**When NOT to use:**
- Need synchronous response (validation, queries)
- Simple request-response patterns
- Strong consistency required
- Low latency critical

**Tradeoffs:**
- Pro: decoupling, independent scaling, resilience
- Con: eventual consistency, harder debugging, message ordering

## Setup

```bash
# Enable APIs
gcloud services enable pubsub.googleapis.com run.googleapis.com

# Create topic
gcloud pubsub topics create user-events

# Deploy services
cd l12-event-driven/terraform
terraform init
terraform apply
```

## Commands

```bash
# Get URLs
export USER_SERVICE_URL=$(gcloud run services describe user-service --region us-central1 --format 'value(status.url)')

# Create user (triggers event)
curl -X POST $USER_SERVICE_URL/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "name": "Test User"
  }'

# Flow:
# 1. User service creates user in Firestore
# 2. Publishes user.created event to Pub/Sub
# 3. Email service receives event → sends welcome email
# 4. Analytics service receives event → tracks signup
# 5. CRM service receives event → creates contact

# Check logs to see all services reacted
gcloud logging read "resource.type=cloud_run_revision AND severity>=INFO" --limit 50
```

## Testing Event Flow

```bash
# Publish event manually
gcloud pubsub topics publish user-events \
  --message '{
    "userId": "test-123",
    "email": "manual@test.com",
    "name": "Manual Test",
    "event": "user.created"
  }'

# All three subscribers should process it
# Check each service logs:
gcloud logging read "resource.labels.service_name=email-service" --limit 10
gcloud logging read "resource.labels.service_name=analytics-service" --limit 10
gcloud logging read "resource.labels.service_name=crm-service" --limit 10
```

## Notes

- At-least-once delivery = may receive duplicate events (idempotency required)
- Push subscriptions = Pub/Sub calls your Cloud Run service
- No ordering guarantee unless using ordering keys
- Dead letter queues for failed messages
- Event schema should be stable (versioning if changes needed)

Cost: $0 (free tier)
