# Lab 07 — Pub/Sub Messaging

## Goal
Event-driven architecture with Pub/Sub. Cloud Run service publishes messages to topic, Pub/Sub pushes them to another endpoint.

## Core Concepts
- **Topic**: Named channel for messages
- **Publisher**: Sends messages to topic
- **Subscription**: Receives messages from topic
- **Push subscription**: Pub/Sub calls your HTTP endpoint with the message
- **At-least-once delivery**: Messages may be delivered multiple times
- **Idempotency**: Handlers should handle duplicate messages gracefully

## Setup

```bash
export REGION=us-central1
export SERVICE=lab07-pubsub
export TOPIC=lab07-topic
export SUBSCRIPTION=lab07-subscription
export PROJECT_ID="$(gcloud config get-value project)"
export SA_NAME=sa-lab07-pubsub
export SA_EMAIL="${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"
```

### 1) Create Pub/Sub Topic
```bash
gcloud pubsub topics create ${TOPIC}

# Verify
gcloud pubsub topics list
```

### 2) Create Service Account
```bash
gcloud iam service-accounts create ${SA_NAME} \
  --display-name="Service Account for ${SERVICE}"

# Grant permission to publish to topic
gcloud pubsub topics add-iam-policy-binding ${TOPIC} \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/pubsub.publisher"
```

### 3) Deploy to Cloud Run
```bash
cd l07-pub-sub

gcloud run deploy ${SERVICE} \
  --source . \
  --region ${REGION} \
  --service-account ${SA_EMAIL} \
  --set-env-vars PROJECT_ID=${PROJECT_ID},PUBSUB_TOPIC=${TOPIC} \
  --allow-unauthenticated
```

### 4) Create Push Subscription
Pub/Sub will POST messages to `/pubsub/consume`:
```bash
SERVICE_URL=$(gcloud run services describe ${SERVICE} --region ${REGION} --format 'value(status.url)')

gcloud pubsub subscriptions create ${SUBSCRIPTION} \
  --topic=${TOPIC} \
  --push-endpoint="${SERVICE_URL}/pubsub/consume" \
  --ack-deadline=60
```

## Test

### Publish a message (producer)
```bash
SERVICE_URL=$(gcloud run services describe ${SERVICE} --region ${REGION} --format 'value(status.url)')

curl -X POST ${SERVICE_URL}/pubsub/publish \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello from Pub/Sub"}'
```

### Check consumption (consumer)
Messages are automatically pushed to `/pubsub/consume`. Check logs:
```bash
gcloud run services logs read ${SERVICE} --region ${REGION} --limit 20
```

You should see "Received Pub/Sub message: Hello from Pub/Sub"

## Endpoints
- `GET /` - Service info
- `POST /pubsub/publish` - Publish message to topic
- `POST /pubsub/consume` - Consumer endpoint (Pub/Sub calls this)

## At-Least-Once Delivery
```bash
# Publish a message
curl -X POST ${SERVICE_URL}/pubsub/publish \
  -H "Content-Type: application/json" \
  -d '{"message": "test-123"}'

# If /pubsub/consume returns non-2xx, Pub/Sub retries
# Check logs - you might see same message multiple times
gcloud run services logs read ${SERVICE} --region ${REGION} | grep "test-123"
```

## Cleanup
```bash
# Delete subscription
gcloud pubsub subscriptions delete ${SUBSCRIPTION}

# Delete topic
gcloud pubsub topics delete ${TOPIC}

# Delete service
gcloud run services delete ${SERVICE} --region ${REGION}

# Delete service account
gcloud iam service-accounts delete ${SA_EMAIL} --quiet
```

## Notes
- **HTTP 2xx = ACK**: Pub/Sub won't retry
- **HTTP != 2xx = NACK**: Pub/Sub will retry (with backoff)
- Messages can arrive out of order
- Same message can be delivered multiple times (design for idempotency)
- Push subscriptions need public endpoints (or proper auth)
- Pub/Sub max message size: 10MB
- Default retention: 7 days

## Alternative: Pull Subscription
Instead of push, can poll for messages:
```bash
# Create pull subscription
gcloud pubsub subscriptions create ${SUBSCRIPTION}-pull \
  --topic=${TOPIC}

# Pull messages manually
gcloud pubsub subscriptions pull ${SUBSCRIPTION}-pull \
  --auto-ack \
  --limit=10
```

## Next
[Lab 08](../l08-ci-cd/) - GitHub Actions CI/CD
