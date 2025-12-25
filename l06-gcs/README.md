# Lab 06 — Cloud Storage (GCS) + IAM + Cloud Run

## Goal
Let Cloud Run service read/write files in a GCS bucket using service account + least-privilege IAM.

## Setup

```bash
export REGION=us-central1
export SERVICE=lab06-gcs
export BUCKET_NAME="your-unique-bucket-name"  # must be globally unique
export PROJECT_ID="$(gcloud config get-value project)"
export SA_NAME=sa-lab06-gcs
export SA_EMAIL="${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"
```

### 1) Create GCS Bucket
```bash
# Create bucket
gcloud storage buckets create gs://${BUCKET_NAME} \
  --location=${REGION}

# Verify
gcloud storage ls
```

### 2) Create Service Account
```bash
gcloud iam service-accounts create ${SA_NAME} \
  --display-name="Service Account for ${SERVICE}"
```

### 3) Grant SA Access to Bucket
Least privilege = only this bucket, not all buckets:
```bash
gcloud storage buckets add-iam-policy-binding gs://${BUCKET_NAME} \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/storage.objectUser"
```

`objectUser` = read + write objects (not delete bucket or change IAM).

### 4) Deploy to Cloud Run
```bash
cd l06-gcs

gcloud run deploy ${SERVICE} \
  --source . \
  --region ${REGION} \
  --service-account ${SA_EMAIL} \
  --set-env-vars BUCKET_NAME=${BUCKET_NAME} \
  --allow-unauthenticated
```

## Test

```bash
SERVICE_URL=$(gcloud run services describe ${SERVICE} --region ${REGION} --format 'value(status.url)')

# Write a file
curl "${SERVICE_URL}/gcs/write?name=test.txt&content=hello+world"

# Read it back
curl "${SERVICE_URL}/gcs/read?name=test.txt"

# List all objects
curl "${SERVICE_URL}/gcs/list"
```

## Endpoints
- `GET /` - Service info
- `GET /gcs/write?name=...&content=...` - Upload file
- `GET /gcs/read?name=...` - Download file
- `GET /gcs/list` - List all objects

## Verify Least Privilege
Try accessing a different bucket (should fail):
```bash
# Create another bucket
gcloud storage buckets create gs://another-bucket-${PROJECT_ID}

# Try to write (will fail - SA only has access to first bucket)
curl "${SERVICE_URL}/gcs/write?name=fail.txt" # won't work
```

## Cleanup
```bash
# Delete service
gcloud run services delete ${SERVICE} --region ${REGION}

# Delete bucket (warning: deletes all objects!)
gcloud storage rm -r gs://${BUCKET_NAME}

# Delete service account
gcloud iam service-accounts delete ${SA_EMAIL} --quiet
```

## Notes
- Bucket names must be globally unique across all GCP
- Use format: `${PROJECT_ID}-something` for uniqueness
- IAM binding is on bucket resource, not project-wide
- Cloud Run uses Application Default Credentials (ADC) automatically
- Objects are stored with full path as name (no real "folders")

## Next
[Lab 07](../l07-pub-sub/) - Pub/Sub messaging
