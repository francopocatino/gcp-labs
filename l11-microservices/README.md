# Lab 11 - Microservices Architecture

Multiple independent services. Each owns its data, communicates via HTTP.

## Architecture

```
User → Order Service → Inventory Service (check stock)
                    → Notification Service (send email)
```

**Services:**
- Order Service - creates orders, orchestrates
- Inventory Service - manages stock
- Notification Service - sends confirmations

**Stack:**
- 3 Cloud Run services
- Firestore (database per service pattern)
- HTTP/REST communication
- Service-to-service auth

## Database per Service

```
order-service      → orders collection
inventory-service  → inventory collection
notification-service → notifications collection
```

Data duplication OK. Each service owns its domain.

## Communication

- Sync HTTP calls between services
- URLs via environment variables
- Cloud Run built-in service auth
- Retry logic with exponential backoff
- Timeout handling (5s default)

## Use Cases

**When to use:**
- Independent scaling needs
- Different teams own different domains
- Deploy services separately
- Mix technologies (Java, Python, Go)

**When NOT to use:**
- Small team (< 5 people)
- Simple domain
- Early prototyping
- Complexity overhead not justified

**Tradeoffs:**
- Pro: independent deployment, fault isolation, tech flexibility
- Con: network latency, distributed complexity, data consistency harder

## Setup

```bash
# Enable APIs
gcloud services enable run.googleapis.com firestore.googleapis.com

# Create Firestore
gcloud firestore databases create --location=us-central1

# Deploy
cd l11-microservices/terraform
terraform init
terraform apply
```

## Commands

```bash
# Get URLs
export ORDER_URL=$(gcloud run services describe order-service --region us-central1 --format 'value(status.url)')
export INVENTORY_URL=$(gcloud run services describe inventory-service --region us-central1 --format 'value(status.url)')
export NOTIFICATION_URL=$(gcloud run services describe notification-service --region us-central1 --format 'value(status.url)')

# Add inventory
curl -X POST $INVENTORY_URL/inventory \
  -H "Content-Type: application/json" \
  -d '{"productId": "widget-123", "quantity": 100}'

# Create order
curl -X POST $ORDER_URL/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": "customer-1", "productId": "widget-123", "quantity": 2}'

# Flow: Order → check inventory → reserve stock → create order → send notification

# Check order
curl $ORDER_URL/orders/{orderId}

# Check inventory (should be 98)
curl $INVENTORY_URL/inventory/widget-123
```

## Testing Failures

```bash
# Inventory service down
gcloud run services update inventory-service --region us-central1 --min-instances 0
curl -X POST $ORDER_URL/orders ...
# Result: graceful error

# Insufficient stock
curl -X POST $ORDER_URL/orders -d '{"productId": "widget-123", "quantity": 1000}'
# Result: 400 error

# Check logs
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=order-service"
```

## Notes

- Each service deploys independently
- No shared database - each owns its data
- Sync HTTP = simpler but slower than async
- Must handle partial failures
- Use Cloud Trace for debugging distributed calls

Cost: $0 (free tier)
