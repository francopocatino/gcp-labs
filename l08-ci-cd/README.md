# Lab 08 — CI/CD with GitHub Actions

## Objective
Auto-deploy to Cloud Run on push to main using GitHub Actions with **keyless auth** (Workload Identity Federation).

## What's Already Set Up

Workflow at `.github/workflows/deploy-cloud-run.yml`:
- Triggers on push to `main`
- Authenticates to GCP via OIDC (no service account keys!)
- Deploys lab02-spring to Cloud Run

## How It Works

```yaml
on:
  push:
    branches: [ "main" ]

permissions:
  contents: read
  id-token: write    # Needed for OIDC

jobs:
  deploy:
    steps:
      - Checkout code
      - Auth to GCP (OIDC)
      - Setup gcloud
      - Deploy to Cloud Run
```

## Workload Identity vs Service Account Keys

**Old way (insecure):**
- Create SA key JSON
- Store in GitHub Secrets
- Risk: keys can leak, never expire

**Workload Identity (secure):**
- GitHub proves identity with OIDC token
- GCP trusts GitHub's token
- Temp credentials issued (~1 hour)
- No keys to leak or rotate

## Setting Up Workload Identity (From Scratch)

If setting this up new:

### 1) Enable APIs
```bash
gcloud services enable iamcredentials.googleapis.com
gcloud services enable sts.googleapis.com
```

### 2) Create Workload Identity Pool
```bash
gcloud iam workload-identity-pools create github-pool \
  --location="global" \
  --description="GitHub Actions Pool"
```

### 3) Create Provider
```bash
gcloud iam workload-identity-pools providers create-oidc github-provider \
  --location="global" \
  --workload-identity-pool="github-pool" \
  --issuer-uri="https://token.actions.githubusercontent.com" \
  --attribute-mapping="google.subject=assertion.sub,attribute.repository=assertion.repository" \
  --attribute-condition="assertion.repository=='YOUR_USERNAME/gcp-labs'"
```

Replace `YOUR_USERNAME/gcp-labs` with your repo.

### 4) Create Service Account
```bash
gcloud iam service-accounts create sa-github-deployer \
  --display-name="GitHub Actions Deployer"
```

### 5) Grant Permissions
```bash
export PROJECT_ID="$(gcloud config get-value project)"
export SA_EMAIL="sa-github-deployer@${PROJECT_ID}.iam.gserviceaccount.com"

# Deploy Cloud Run
gcloud projects add-iam-policy-binding ${PROJECT_ID} \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/run.admin"

# Act as runtime SA
gcloud projects add-iam-policy-binding ${PROJECT_ID} \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/iam.serviceAccountUser"
```

### 6) Allow GitHub to Impersonate SA
```bash
export PROJECT_NUMBER=$(gcloud projects describe ${PROJECT_ID} --format="value(projectNumber)")
export REPO="YOUR_USERNAME/gcp-labs"

gcloud iam service-accounts add-iam-policy-binding ${SA_EMAIL} \
  --member="principalSet://iam.googleapis.com/projects/${PROJECT_NUMBER}/locations/global/workloadIdentityPools/github-pool/attribute.repository/${REPO}" \
  --role="roles/iam.workloadIdentityUser"
```

### 7) Get Provider Name
```bash
gcloud iam workload-identity-pools providers describe github-provider \
  --location="global" \
  --workload-identity-pool="github-pool" \
  --format="value(name)"
```

Use this in workflow's `workload_identity_provider` field.

## Test It

```bash
# Make a change
echo "# test" >> test.md
git add test.md
git commit -m "test: ci/cd"
git push origin main

# Watch: GitHub → Actions tab
# Verify: gcloud run services describe lab02-spring --region us-central1
```

## Extending the Workflow

Add before deploy:
```yaml
- name: Run tests
  run: |
    cd l02-cloud-run-spring/app
    mvn test

- name: Security scan
  uses: aquasecurity/trivy-action@master
  with:
    scan-type: 'fs'
    scan-ref: 'l02-cloud-run-spring'
```

## Why Workload Identity
- ✅ No service account keys
- ✅ Credentials auto-expire
- ✅ Restrict by repo/branch
- ✅ Full audit trail
- ✅ Zero-trust security

## Notes
- Workflow runs on every push to main
- Can add path filters to only run when app code changes
- Can add manual approval for prod deploys
- View logs: GitHub → Actions → click run
- IAM changes take 1-2 min to propagate

## Cleanup
```bash
# Delete SA
gcloud iam service-accounts delete sa-github-deployer@${PROJECT_ID}.iam.gserviceaccount.com

# Delete provider
gcloud iam workload-identity-pools providers delete github-provider \
  --location="global" \
  --workload-identity-pool="github-pool"

# Delete pool
gcloud iam workload-identity-pools delete github-pool --location="global"
```

---

**Labs complete!** You now know: Cloud Run, secrets, IAM, storage, messaging, and automated deployments.
