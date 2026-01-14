# Architecture

How the labs connect and what they do.

## Overall Flow

```
GitHub
  │
  ├─ push to main
  │
  ▼
GitHub Actions (OIDC auth)
  │
  ├─ test
  ├─ security scan
  ├─ deploy to Cloud Run
  │
  ▼
Cloud Run Services (serverless)
  │
  ├─ lab02-spring  → reads Secret Manager
  ├─ lab06-gcs     → reads/writes Cloud Storage
  └─ lab07-pubsub  → publishes to Pub/Sub
```

## Infrastructure (Terraform)

Everything created with `terraform apply`:

- **Service Accounts**: Each service has its own identity
- **IAM Bindings**: Least-privilege permissions
- **Secrets**: Stored in Secret Manager (not in code)
- **Storage**: GCS bucket for file uploads
- **Messaging**: Pub/Sub topic for async messages
- **CI/CD**: Workload Identity for keyless GitHub auth

## Services

### Lab 02 - Basic Cloud Run
- Spring Boot REST API
- Reads env vars (config)
- Can read secrets from Secret Manager
- Auto-scales, pay per use

### Lab 06 - Cloud Storage
- Separate service with GCS operations
- Upload/download/list files
- Uses service account for auth
- Bucket created by Terraform

### Lab 07 - Pub/Sub
- Async messaging
- Publish endpoint (sends messages)
- Consume endpoint (receives via push)
- At-least-once delivery

## Deployment

Push to main triggers:
1. Tests run (Maven)
2. Security scan (Trivy)
3. Build Docker image
4. Deploy to Cloud Run
5. Smoke test (curl /health)

All automatic, no manual steps.

## Local Development

```bash
# Run locally
cd l02-cloud-run-spring/app
mvn spring-boot:run

# Test
mvn test

# Deploy manually
gcloud run deploy lab02-spring --source .
```

## Why Separate Services?

Could've put everything in one big service, but:
- Each lab teaches one concept
- Easier to understand
- Can deploy independently
- More realistic (microservices)

## Cost

Mostly free tier. See TROUBLESHOOTING.md for details.
