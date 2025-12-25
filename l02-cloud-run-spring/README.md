# Lab 02 — Cloud Run + Spring Boot

Basic Spring Boot REST API deployed to Cloud Run. Learning serverless deployment.

## What I Built

Simple REST service with:
- `GET /` - Returns service info + env vars
- `GET /health` - Health check

## Deployment

```bash
export REGION=us-central1
export SERVICE=lab02-spring

gcloud run deploy ${SERVICE} \
  --source . \
  --region ${REGION} \
  --allow-unauthenticated \
  --set-env-vars APP_NAME="GCP Labs",MESSAGE="Hello from Cloud Run"
```

Cloud Run builds the container automatically from the Dockerfile (multi-stage build).

## Local Testing

```bash
cd app
mvn package
java -jar target/*.jar
curl http://localhost:8080/
```

## Observations

- **Scale to zero**: No traffic = no instances = no charges
- **Cold start**: ~200-500ms on first request after idle
- **Auto-scaling**: Handles traffic spikes without config

Left it idle for 15 min then hit it - definitely felt the cold start.

## Config

App reads env vars (APP_NAME, MESSAGE, SPRING_PROFILES_ACTIVE). Can update these without rebuilding - see lab04.

## Cleanup

```bash
gcloud run services delete ${SERVICE} --region ${REGION}
```
