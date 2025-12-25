# Lab 05 — Secrets + IAM (Cloud Run + Secret Manager)

## Goal
Store API keys/secrets in Secret Manager (not env vars or code). Use dedicated service account with least-privilege IAM.

## Core Concepts
- **Secret**: Sensitive data (API keys, passwords) that shouldn't be in code/logs
- **Secret Manager**: GCP's managed secret storage (encrypted, versioned, audited)
- **Service Account**: Non-human identity for Cloud Run to run as
- **Least Privilege**: Grant minimum permissions needed on minimum resources

## Setup

```bash
export REGION=us-central1
export SERVICE=lab02-spring
export SA_NAME=sa-lab02-spring
export SECRET_ID=lab02_api_key
export PROJECT_ID="$(gcloud config get-value project)"
export SA_EMAIL="${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"
```

### 1) Create Secret
```bash
# Enable API
gcloud services enable secretmanager.googleapis.com

# Create secret with a value
echo -n "my-super-secret-api-key-12345" | \
  gcloud secrets create ${SECRET_ID} \
    --data-file=- \
    --replication-policy="automatic"

# Verify
gcloud secrets describe ${SECRET_ID}
```

### 2) Create Service Account
```bash
gcloud iam service-accounts create ${SA_NAME} \
  --display-name="Service Account for ${SERVICE}"

# Verify
gcloud iam service-accounts list --filter="email:${SA_EMAIL}"
```

### 3) Grant SA Access to Secret
Least privilege = only this secret, not all secrets:
```bash
gcloud secrets add-iam-policy-binding ${SECRET_ID} \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/secretmanager.secretAccessor"

# Verify
gcloud secrets get-iam-policy ${SECRET_ID}
```

### 4) Update Cloud Run to Use SA and Secret
```bash
gcloud run services update ${SERVICE} \
  --region ${REGION} \
  --service-account ${SA_EMAIL} \
  --update-secrets API_KEY=${SECRET_ID}:latest
```

This mounts the secret as `API_KEY` env var.

## Test
```bash
SERVICE_URL=$(gcloud run services describe ${SERVICE} --region ${REGION} --format 'value(status.url)')
curl ${SERVICE_URL}/
```

App can read the secret internally (but shouldn't log it).

## Update Secret Value
```bash
# Add new version
echo -n "new-secret-value" | gcloud secrets versions add ${SECRET_ID} --data-file=-

# Redeploy to pick it up (using :latest)
gcloud run services update ${SERVICE} --region ${REGION}
```

## Cleanup
```bash
gcloud secrets delete ${SECRET_ID} --quiet
gcloud iam service-accounts delete ${SA_EMAIL} --quiet
```

## Why This Matters
- ✅ Secrets not in code (can't leak via git)
- ✅ Secrets not in env vars (visible in console)
- ✅ Encrypted at rest
- ✅ Audit trail of access
- ✅ Easy rotation (new version)
- ✅ Scoped permissions (only this secret)

vs hardcoding:
- ❌ Leaked on GitHub
- ❌ Can't rotate without code change
- ❌ Same key everywhere (dev/prod)

## Notes
- IAM binding is on the secret resource, not project-wide
- `:latest` auto-picks newest version
- Can pin to specific version: `${SECRET_ID}:1`
- Service account has no permissions except accessing this one secret
- View in console: Secret Manager → click secret → see versions & IAM

## Next
[Lab 06](../l06-gcs/) - Cloud Storage
