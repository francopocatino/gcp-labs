# Lab 13 - API Gateway Pattern

Single entry point for multiple backend services. Routes, authenticates, transforms requests.

## Architecture

```
External Clients
    ↓
API Gateway (single endpoint)
    ├─→ /users → User Service
    ├─→ /products → Product Service
    └─→ /orders → Order Service
```

**Pattern:**
- One public URL
- Routes to multiple backends
- Authentication at gateway level
- Rate limiting
- Request/response transformation
- Versioning support

**Stack:**
- API Gateway (managed service)
- 3 Cloud Run backend services
- OpenAPI spec for routing
- API keys for authentication

## API Gateway vs Direct Access

| Aspect | Direct | API Gateway |
|--------|--------|-------------|
| Endpoints | Multiple URLs | Single URL |
| Auth | Each service | Centralized |
| Rate Limiting | Per service | Centralized |
| Monitoring | Separate | Unified |
| Versioning | Manual | Built-in |
| CORS | Each service | Centralized |

## Use Cases

**When to use:**
- Expose multiple services externally
- Need centralized auth/rate limiting
- API versioning required
- External partners/developers consume API
- Want to hide backend complexity

**When NOT to use:**
- Internal service-to-service calls (use direct HTTP)
- Simple single service
- Need very low latency (gateway adds overhead)
- Complex transformations (use service mesh instead)

**Tradeoffs:**
- Pro: single entry point, centralized control, simplified client
- Con: added latency, single point of failure, additional cost

## Setup

```bash
# Enable API
gcloud services enable apigateway.googleapis.com

# Deploy backend services
cd l13-api-gateway/terraform
terraform init
terraform apply

# Deploy API Gateway config
gcloud api-gateway api-configs create my-api-config \
  --api=my-api \
  --openapi-spec=api-spec.yaml \
  --backend-auth-service-account=api-gateway-sa@PROJECT_ID.iam.gserviceaccount.com

# Deploy gateway
gcloud api-gateway gateways create my-gateway \
  --api=my-api \
  --api-config=my-api-config \
  --location=us-central1
```

## Commands

```bash
# Get gateway URL
export GATEWAY_URL=$(gcloud api-gateway gateways describe my-gateway --location=us-central1 --format='value(defaultHostname)')

# Test routing
curl https://$GATEWAY_URL/users
curl https://$GATEWAY_URL/products
curl https://$GATEWAY_URL/orders

# With API key
curl https://$GATEWAY_URL/users?key=YOUR_API_KEY

# Check logs
gcloud logging read "resource.type=api" --limit 20
```

## OpenAPI Spec Example

```yaml
swagger: "2.0"
info:
  title: "My API"
  version: "1.0.0"
schemes:
  - "https"
produces:
  - "application/json"
paths:
  /users:
    get:
      summary: "List users"
      operationId: "getUsers"
      x-google-backend:
        address: https://user-service-xxx.run.app
      responses:
        200:
          description: "Success"
  /products:
    get:
      summary: "List products"
      operationId: "getProducts"
      x-google-backend:
        address: https://product-service-xxx.run.app
      responses:
        200:
          description: "Success"
```

## Notes

- API Gateway adds ~100-200ms latency
- Costs $3/million requests (after free tier)
- OpenAPI 2.0 spec required (not 3.0)
- Backend services must allow gateway service account
- Rate limits set per API key
- Can enable CORS at gateway level

Cost: ~$0 for learning (free tier: 2M calls/month)
