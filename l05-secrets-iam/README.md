# Lab 05 — Secrets + IAM (Cloud Run + Secret Manager)

## Goal
Store a sensitive value in Secret Manager and allow a Cloud Run service to access it using a dedicated Service Account with **least privilege**.

## Core Concepts
- **Secret**: Sensitive configuration (API keys, tokens, passwords) that must not be committed to Git, baked into container images, or printed in logs.
- **Secret Manager**: Managed secret storage with encryption, auditing, and versioning.
- **IAM (Identity and Access Management)**: Defines **who** (principal) can do **what** (role/permissions) on **which resource**.
- **Service Account (SA)**: Non-human identity used by workloads (Cloud Run, VMs, etc.).
- **Least Privilege**: Grant only the minimum permissions required, scoped to the minimum resources required.

## Built
- A Secret Manager secret: `lab02_api_key`
- A dedicated Service Account for the Cloud Run service
- A resource-scoped IAM policy binding granting:
  - `roles/secretmanager.secretAccessor` **only** on the specific secret
- A Cloud Run service configured to:
  - run as the dedicated Service Account
  - inject the secret as an environment variable via `--update-secrets`

## Commands

### Set variables
```bash
export REGION=us-central1
export SERVICE=lab02-spring
export SA_NAME=sa-lab02-spring
export SECRET_ID=lab02_api_key
export PROJECT_ID="$(gcloud config get-value project)"
export SA_EMAIL="${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"

