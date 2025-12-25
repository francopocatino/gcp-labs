# Lab 01 — Basics (GCP Setup)

## Objective
Get local + cloud environment ready:
- GCP account and project
- Billing alerts (don't want surprise charges)
- gcloud CLI auth and config

## Steps

### 1) Create GCP Project
- Go to [console.cloud.google.com](https://console.cloud.google.com)
- New Project → pick a name
- Note the Project ID (need this later)

### 2) Set Up Billing
Link billing account in console, then set up budget alerts:
- Billing → Budgets & alerts
- Set budget ($5-10/month is safe for learning)
- Alerts at 50%, 90%, 100%

This saved me once when I forgot to delete a Cloud SQL instance.

### 3) Authenticate gcloud
```bash
gcloud auth login
gcloud auth list  # verify you're logged in
```

### 4) Configure Default Project
Either use the script:
```bash
cd scripts
./init-gcloud.sh YOUR_PROJECT_ID
```

Or manually:
```bash
gcloud config set project YOUR_PROJECT_ID
gcloud config list  # check it worked
```

### 5) Verify Setup
```bash
cd scripts
./check.sh
```

Should show your account, project, and enabled services.

## Notes
- Project ID ≠ project name (ID is the one in parentheses)
- If `gcloud` command not found, restart terminal after installing SDK
- Free tier is generous but still set billing alerts

## Next
[Lab 02](../l02-cloud-run-spring/) - Deploy first app to Cloud Run
