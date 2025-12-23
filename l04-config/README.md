# Lab 04 — Configuration via Environment Variables

## Objective
Change application behavior using environment variables in Cloud Run
without rebuilding the container image.

## Changes
- Application reads env vars (APP_NAME, MESSAGE, SPRING_PROFILES_ACTIVE)
- Cloud Run service updated via configuration only

## Key concepts
- Immutable images
- Mutable configuration
- Revisions as snapshots

## Commands
```bash
gcloud run services update lab02-spring --set-env-vars ...

