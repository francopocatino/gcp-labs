# Lab 03 — Observability and Runtime Behavior (Cloud Run)

## Objective
Understand how a Cloud Run service behaves at runtime under traffic and idle conditions.

## Scope
This lab focuses on observing an existing service without modifying code or infrastructure.

## Observations
- Cloud Run scales instances to zero when idle
- First request after idle triggers a cold start
- Subsequent requests are served by warm instances
- Logs are centralized and accessible without SSH

## Commands used
```bash
gcloud run services logs read lab02-spring --region us-central1
curl https://<service-url>


and
gcloud beta run services logs tail lab02-spring --region us-central1

