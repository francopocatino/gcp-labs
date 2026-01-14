#!/bin/bash

# Environment validation script for GCP Labs
# Checks prerequisites before running labs

set -e

echo "🔍 Validating GCP environment setup..."
echo ""

# Check gcloud installed
if ! command -v gcloud &> /dev/null; then
    echo "❌ gcloud CLI not found. Install from: https://cloud.google.com/sdk/docs/install"
    exit 1
fi
echo "✅ gcloud CLI installed"

# Check authentication
if ! gcloud auth list --filter=status:ACTIVE --format="value(account)" &> /dev/null; then
    echo "❌ Not authenticated. Run: gcloud auth login"
    exit 1
fi
ACTIVE_ACCOUNT=$(gcloud auth list --filter=status:ACTIVE --format="value(account)")
echo "✅ Authenticated as: $ACTIVE_ACCOUNT"

# Check project configured
PROJECT_ID=$(gcloud config get-value project 2> /dev/null)
if [ -z "$PROJECT_ID" ]; then
    echo "❌ No project configured. Run: gcloud config set project YOUR_PROJECT_ID"
    exit 1
fi
echo "✅ Project configured: $PROJECT_ID"

# Check required APIs are enabled (common ones)
echo ""
echo "📋 Checking required APIs..."

REQUIRED_APIS=(
    "run.googleapis.com"
    "cloudbuild.googleapis.com"
    "secretmanager.googleapis.com"
    "storage.googleapis.com"
    "pubsub.googleapis.com"
    "sqladmin.googleapis.com"
)

MISSING_APIS=()

for api in "${REQUIRED_APIS[@]}"; do
    if gcloud services list --enabled --filter="name:$api" --format="value(name)" 2>/dev/null | grep -q "$api"; then
        echo "  ✅ $api"
    else
        echo "  ❌ $api (not enabled)"
        MISSING_APIS+=("$api")
    fi
done

if [ ${#MISSING_APIS[@]} -gt 0 ]; then
    echo ""
    echo "⚠️  Some APIs are not enabled. Enable them with:"
    echo "gcloud services enable ${MISSING_APIS[@]}"
    echo ""
fi

# Check for Java and Maven (for Spring Boot labs)
echo ""
echo "🔧 Checking development tools..."

if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    echo "✅ Java installed: $JAVA_VERSION"
else
    echo "⚠️  Java not found (required for Spring Boot labs)"
fi

if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1 | cut -d' ' -f3)
    echo "✅ Maven installed: $MVN_VERSION"
else
    echo "⚠️  Maven not found (required for Spring Boot labs)"
fi

# Check for Terraform
if command -v terraform &> /dev/null; then
    TF_VERSION=$(terraform version -json | grep -o '"terraform_version":"[^"]*"' | cut -d'"' -f4)
    echo "✅ Terraform installed: $TF_VERSION"
else
    echo "⚠️  Terraform not found (optional, for IaC management)"
fi

echo ""
echo "✨ Validation complete!"
