# Lab 02 — Cloud Run + Spring App Sample

## Objective
Deploy a basic Spring Boot REST API to Cloud Run. Understand serverless (scale-to-zero, auto-scaling, pay-per-use).

## Endpoints
- `GET /`        → status + env vars
- `GET /health`  → health check

## Local Testing
```bash
cd app
mvn -DskipTests package
java -jar target/*.jar
# test: curl http://localhost:8080/
```

Works locally before deploying.

## Deploy to Cloud Run

```bash
export REGION=us-central1
export SERVICE=lab02-spring

cd l02-cloud-run-spring

gcloud run deploy ${SERVICE} \
  --source . \
  --region ${REGION} \
  --allow-unauthenticated \
  --set-env-vars APP_NAME="GCP Labs",MESSAGE="Hello from Cloud Run",SPRING_PROFILES_ACTIVE=prod
```

Takes 2-3 min. Cloud Run builds the container automatically using the Dockerfile.

## Test
```bash
SERVICE_URL=$(gcloud run services describe ${SERVICE} --region ${REGION} --format 'value(status.url)')
curl ${SERVICE_URL}/
```

## Key Behaviors
- **Scale to zero**: No traffic = no instances = no charges
- **Cold start**: First request after idle takes longer (~200-500ms)
- **Auto-scale**: Handles traffic spikes automatically

Wait 15 min without traffic, then curl again - you'll feel the cold start.

## Env Variables
App reads:
- `APP_NAME` - app name shown in response
- `MESSAGE` - custom message
- `SPRING_PROFILES_ACTIVE` - Spring profile

Can update these without rebuilding (see lab04).

## Cleanup
```bash
gcloud run services delete ${SERVICE} --region ${REGION}
```

## Notes
- `--source .` uses Dockerfile for build (multi-stage: Maven → JRE)
- `--allow-unauthenticated` makes it public (fine for learning)
- Logs available in Cloud Console → Cloud Run → service → Logs tab
- Image goes to Artifact Registry automatically

## Next
[Lab 03](../l03-observability/) - Logging and monitoring
