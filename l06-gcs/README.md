# Lab 06 — Cloud Storage (GCS) + IAM + Cloud Run

## Goal
Allow a Cloud Run service to read and write objects in a Cloud Storage bucket
using a dedicated Service Account and least-privilege IAM.

## Core Concepts
- Bucket vs Object
- Resource-level IAM
- Service Account per workload
- Least Privilege

## Built
- One GCS bucket
- One Cloud Run service
- IAM binding at bucket level only

## Permissions
- roles/storage.objectAdmin on the specific bucket
- No project-wide permissions

## Verification
- Positive: write/read object succeeds
- Negative: removing IAM binding causes failures

## Notes
- Buckets are globally named
- Cloud Run uses Application Default Credentials

