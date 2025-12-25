# GCP Learning Labs

Hands-on labs for learning Google Cloud Platform. Each lab builds on the previous one.

## Labs

| Lab | Topic | Status |
|-----|-------|--------|
| [01](l01-basics/) | GCP Setup & Auth | ✓ |
| [02](l02-cloud-run-spring/) | Cloud Run + Spring Boot | ✓ |
| [03](l03-observability/) | Logging & Monitoring | ✓ |
| [04](l04-config/) | Environment Variables | ✓ |
| [05](l05-secrets-iam/) | Secret Manager + IAM | ✓ |
| [06](l06-gcs/) | Cloud Storage | ✓ |
| [07](l07-pub-sub/) | Pub/Sub Messaging | ✓ |
| [08](l08-ci-cd/) | GitHub Actions CI/CD | ✓ |

## Prerequisites

- GCP account (free tier works)
- gcloud CLI installed
- Java 17+ and Maven
- Basic command line knowledge

## Setup

```bash
git clone <repo-url>
cd gcp-labs
```

Start with lab01 to configure gcloud, then work through sequentially.

## Cost Management

Most services have free tiers, but watch out for:
- Cloud Run: Free tier is generous (2M requests/month)
- Cloud Storage: First 5GB free
- Pub/Sub: First 10GB free

**Important**: Delete resources when done with each lab to avoid charges.

## Project Structure

```
gcp-labs/
├── l01-basics/              # GCP setup
├── l02-cloud-run-spring/    # Spring Boot app (main codebase)
├── l03-observability/       # Logging examples
├── l04-config/              # Environment config
├── l05-secrets-iam/         # Secrets & service accounts
├── l06-gcs/                 # Cloud Storage
├── l07-pub-sub/             # Event messaging
└── l08-ci-cd/               # GitHub Actions
```

## Security Note

This is a public repo. Never commit:
- GCP project IDs
- Service account keys
- Actual secrets or API keys

Use `.env.example` as a template for local config.

## Quick Reference

```bash
# Common commands
gcloud config get-value project
gcloud auth list
gcloud run services list --region us-central1
gcloud run services delete SERVICE --region us-central1
```

## Next Steps After Completing

- Build a multi-service app using these patterns
- Try Cloud SQL for databases
- Explore Terraform for infrastructure as code
- Add Cloud Monitoring and alerting
