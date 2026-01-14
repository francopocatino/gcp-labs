# Lab 02 — Cloud Run + Spring Boot

Basic Spring Boot REST API deployed to Cloud Run.

## Endpoints

- `GET /` - Service info + env vars
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

## Notes

- Scale to zero when no traffic
- Cold start latency on first request after idle
- Auto-scaling based on traffic
- Environment variables configurable without rebuild (see lab04)

## Cleanup

```bash
gcloud run services delete ${SERVICE} --region ${REGION}
```
