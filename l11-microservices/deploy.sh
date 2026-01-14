#!/bin/bash

set -e

PROJECT_ID=${1:-$(gcloud config get-value project)}
REGION=${2:-us-central1}
REPO_NAME="cloud-run-apps"

echo "Deploying microservices to project: $PROJECT_ID"

# Enable APIs
echo "Enabling required APIs..."
gcloud services enable \
  run.googleapis.com \
  artifactregistry.googleapis.com \
  firestore.googleapis.com \
  cloudbuild.googleapis.com \
  --project=$PROJECT_ID

# Create Firestore database if it doesn't exist
echo "Checking Firestore database..."
gcloud firestore databases create --location=$REGION --project=$PROJECT_ID 2>/dev/null || echo "Firestore already exists"

# Build and push images
echo "Building and pushing Docker images..."

cd order-service
gcloud builds submit --tag $REGION-docker.pkg.dev/$PROJECT_ID/$REPO_NAME/order-service:latest --project=$PROJECT_ID
cd ..

cd inventory-service
gcloud builds submit --tag $REGION-docker.pkg.dev/$PROJECT_ID/$REPO_NAME/inventory-service:latest --project=$PROJECT_ID
cd ..

cd notification-service
gcloud builds submit --tag $REGION-docker.pkg.dev/$PROJECT_ID/$REPO_NAME/notification-service:latest --project=$PROJECT_ID
cd ..

# Deploy with Terraform
echo "Deploying services with Terraform..."
cd terraform
terraform init
terraform apply -var="project_id=$PROJECT_ID" -var="region=$REGION" -auto-approve

echo ""
echo "Deployment complete!"
echo ""
terraform output -raw deploy_commands
