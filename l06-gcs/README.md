# Lab 06 — Cloud Storage + IAM

Cloud Run service with GCS integration. Service account scoped to specific bucket.

## Endpoints

- `GET /gcs/write?name=...&content=...` - Upload file
- `GET /gcs/read?name=...` - Download file
- `GET /gcs/list` - List all objects

## Setup

```bash
export REGION=us-central1
export SERVICE=lab06-gcs
export BUCKET_NAME="my-unique-bucket-name"
export PROJECT_ID="$(gcloud config get-value project)"
export SA_NAME=sa-lab06-gcs
export SA_EMAIL="${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"

# Create bucket
gcloud storage buckets create gs://${BUCKET_NAME} --location=${REGION}

# Create service account
gcloud iam service-accounts create ${SA_NAME}

# Grant bucket-level access
gcloud storage buckets add-iam-policy-binding gs://${BUCKET_NAME} \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/storage.objectUser"

# Deploy
cd l06-gcs
gcloud run deploy ${SERVICE} \
  --source . \
  --region ${REGION} \
  --service-account ${SA_EMAIL} \
  --set-env-vars BUCKET_NAME=${BUCKET_NAME} \
  --allow-unauthenticated
```

## Testing

```bash
SERVICE_URL=$(gcloud run services describe ${SERVICE} --region ${REGION} --format 'value(status.url)')

curl "${SERVICE_URL}/gcs/write?name=test.txt&content=hello"
curl "${SERVICE_URL}/gcs/read?name=test.txt"
curl "${SERVICE_URL}/gcs/list"
```

## Notes

- IAM binding at bucket level (not project-wide)
- `objectUser` role provides read/write access
- Uses Application Default Credentials
- Bucket names must be globally unique

## Cleanup

```bash
gcloud run services delete ${SERVICE} --region ${REGION}
gcloud storage rm -r gs://${BUCKET_NAME}
gcloud iam service-accounts delete ${SA_EMAIL} --quiet
```
