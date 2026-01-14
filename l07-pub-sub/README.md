# Lab 07 — Pub/Sub Messaging

Event-driven messaging with Pub/Sub. Publisher and consumer using push subscriptions.

## Endpoints

- `POST /pubsub/publish` - Publish message to topic
- `POST /pubsub/consume` - Consumer endpoint (Pub/Sub calls this)

## Setup

```bash
export REGION=us-central1
export SERVICE=lab07-pubsub
export TOPIC=lab07-topic
export SUBSCRIPTION=lab07-subscription
export PROJECT_ID="$(gcloud config get-value project)"
export SA_NAME=sa-lab07-pubsub
export SA_EMAIL="${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"

# Create topic
gcloud pubsub topics create ${TOPIC}

# Create service account with publish permission
gcloud iam service-accounts create ${SA_NAME}
gcloud pubsub topics add-iam-policy-binding ${TOPIC} \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/pubsub.publisher"

# Deploy
cd l07-pub-sub
gcloud run deploy ${SERVICE} \
  --source . \
  --region ${REGION} \
  --service-account ${SA_EMAIL} \
  --set-env-vars PROJECT_ID=${PROJECT_ID},PUBSUB_TOPIC=${TOPIC} \
  --allow-unauthenticated

# Create push subscription
SERVICE_URL=$(gcloud run services describe ${SERVICE} --region ${REGION} --format 'value(status.url)')
gcloud pubsub subscriptions create ${SUBSCRIPTION} \
  --topic=${TOPIC} \
  --push-endpoint="${SERVICE_URL}/pubsub/consume"
```

## Testing

```bash
# Publish message
curl -X POST ${SERVICE_URL}/pubsub/publish \
  -H "Content-Type: application/json" \
  -d '{"message": "test message"}'

# Check logs for consumption
gcloud run services logs read ${SERVICE} --region ${REGION} --limit 20
```

## Notes

- HTTP 2xx = ACK (Pub/Sub won't retry)
- HTTP != 2xx = NACK (Pub/Sub retries with backoff)
- At-least-once delivery (messages may arrive multiple times)
- No guaranteed ordering unless configured
- Push subscriptions for this use case

## Cleanup

```bash
gcloud pubsub subscriptions delete ${SUBSCRIPTION}
gcloud pubsub topics delete ${TOPIC}
gcloud run services delete ${SERVICE} --region ${REGION}
gcloud iam service-accounts delete ${SA_EMAIL} --quiet
```
