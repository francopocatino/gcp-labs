#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <PROJECT_ID>"
  exit 1
fi

PROJECT_ID="$1"

echo "Setting default project to: $PROJECT_ID"
gcloud config set project "$PROJECT_ID"

echo "Current config:"
gcloud config list

echo "Active account:"
gcloud auth list --filter=status:ACTIVE --format="value(account)"

echo "Done."

