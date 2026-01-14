#!/usr/bin/env bash
set -euo pipefail

echo "== gcloud version =="
gcloud version | head -n 1

echo
echo "== Active account =="
gcloud auth list --filter=status:ACTIVE --format="value(account)"

echo
echo "== Current project =="
gcloud config get-value project

PROJECT_ID="$(gcloud config get-value project)"

echo
echo "== Describe project ($PROJECT_ID) =="
gcloud projects describe "$PROJECT_ID" --format="yaml(projectId,name,projectNumber,lifecycleState)"

echo
echo "== Enabled services (first 30) =="
gcloud services list --enabled --format="value(config.name)" | head -n 30

echo
echo "OK"
