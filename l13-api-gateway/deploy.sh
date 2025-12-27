#!/bin/bash

set -e

PROJECT_ID=${1:-$(gcloud config get-value project)}
REGION=${2:-us-central1}

echo "Deploying API Gateway demo to project: $PROJECT_ID"

# Enable APIs
terraform init
terraform apply -var="project_id=$PROJECT_ID" -auto-approve

# Deploy backend service (reference existing or deploy simple one)
echo "Deploy a backend Cloud Run service first, then update api-spec.yaml with its URL"
echo ""
echo "Example:"
echo "gcloud run deploy backend-service --image gcr.io/cloudrun/hello --region $REGION"
echo ""
echo "Then create API:"
echo "gcloud api-gateway apis create my-api --project=$PROJECT_ID"
echo ""
echo "Upload config:"
echo "gcloud api-gateway api-configs create my-config --api=my-api --openapi-spec=api-spec.yaml --project=$PROJECT_ID"
echo ""
echo "Create gateway:"
echo "gcloud api-gateway gateways create my-gateway --api=my-api --api-config=my-config --location=$REGION --project=$PROJECT_ID"
