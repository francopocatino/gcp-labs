# GCP Learning Labs

Personal hands-on labs for learning Google Cloud Platform. Built these while learning GCP fundamentals.

## Labs

| Lab | Topic | Notes |
|-----|-------|-------|
| [01](l01-basics/) | GCP Setup & Auth | Initial setup, gcloud config |
| [02](l02-cloud-run-spring/) | Cloud Run + Spring Boot | Basic serverless deployment |
| [03](l03-observability/) | Logging & Monitoring | Cloud Logging, cold starts |
| [04](l04-config/) | Environment Variables | Revisions, config mgmt |
| [05](l05-secrets-iam/) | Secret Manager + IAM | Secrets, service accounts, least privilege |
| [06](l06-gcs/) | Cloud Storage | Object storage with IAM |
| [07](l07-pub-sub/) | Pub/Sub Messaging | Event-driven messaging |
| [08](l08-ci-cd/) | GitHub Actions CI/CD | OIDC, Workload Identity |
| [09](l09-cloud-sql/) | Cloud SQL + PostgreSQL | Managed database with Cloud Run |
| [10](l10-observability/) | Logging & Monitoring | Cloud Logging, Error Reporting, Metrics |

## Stack

- Java 17 + Spring Boot
- Maven
- Docker (multi-stage builds)
- GCP services: Cloud Run, Secret Manager, GCS, Pub/Sub, Cloud SQL
- GitHub Actions for CI/CD

## Structure

```
gcp-labs/
├── l01-basics/              # GCP setup
├── l02-cloud-run-spring/    # Basic Spring Boot service
├── l03-observability/       # Logging examples
├── l04-config/              # Environment config
├── l05-secrets-iam/         # Secrets & service accounts
├── l06-gcs/                 # Cloud Storage
├── l07-pub-sub/             # Event messaging
├── l08-ci-cd/               # GitHub Actions
├── l09-cloud-sql/           # Cloud SQL + PostgreSQL
└── l10-observability/       # Logging & monitoring guide
```

## Key Learnings

- Serverless deployment with Cloud Run (scale-to-zero, pay-per-use)
- IAM best practices (least privilege, resource-scoped permissions)
- Keyless auth with Workload Identity (no service account keys)
- Event-driven architecture with Pub/Sub
- Infrastructure security (Secret Manager, proper IAM)

## Running Locally

```bash
cd l02-cloud-run-spring/app
mvn package
java -jar target/*.jar
```

## Notes

Built for learning GCP. Each lab is independently deployable. Used `.env.example` for config templates to avoid committing secrets.
