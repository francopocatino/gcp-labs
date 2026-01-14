# Terraform - Infrastructure as Code

Terraform configuration for GCP labs infrastructure. Replaces manual `gcloud` commands from labs 05-08.

## What It Creates

- **Lab 05**: Secret Manager secret + service account + IAM binding
- **Lab 06**: GCS bucket + service account + IAM binding
- **Lab 07**: Pub/Sub topic + service account + IAM binding
- **Lab 08**: Workload Identity Pool/Provider + GitHub Actions service account

## Prerequisites

- [Terraform](https://www.terraform.io/downloads) installed (>= 1.0)
- gcloud CLI authenticated: `gcloud auth application-default login`
- GCP project with billing enabled

## Setup

1. **Copy variables template:**
   ```bash
   cp terraform.tfvars.example terraform.tfvars
   ```

2. **Edit `terraform.tfvars` with your values:**
   ```hcl
   project_id  = "your-gcp-project-id"
   region      = "us-central1"
   github_repo = "your-username/gcp-labs"
   bucket_name = "your-project-id-lab06-bucket"  # Must be globally unique
   ```

3. **Initialize Terraform:**
   ```bash
   terraform init
   ```

## Usage

### Preview changes
```bash
terraform plan
```

Shows what Terraform will create/modify/destroy.

### Apply changes
```bash
terraform apply
```

Creates all infrastructure. Type `yes` to confirm.

### View outputs
```bash
terraform output
```

Shows service account emails, bucket name, Workload Identity provider, etc.

### Destroy everything
```bash
terraform destroy
```

Deletes all Terraform-managed resources. Type `yes` to confirm.

## Files

- `providers.tf` - GCP provider configuration
- `variables.tf` - Input variables (project_id, region, etc.)
- `terraform.tfvars` - Actual values (gitignored)
- `lab05-secrets.tf` - Secret Manager resources
- `lab06-gcs.tf` - Cloud Storage resources
- `lab07-pubsub.tf` - Pub/Sub resources
- `lab08-cicd.tf` - Workload Identity Federation
- `outputs.tf` - Display created resource info

## Notes

- State stored locally in `terraform.tfstate` (gitignored)
- Run `terraform plan` before `apply` to preview changes
- Use `terraform destroy` to clean up when done
- Bucket has `force_destroy = true` (deletes all objects on destroy)

## After Terraform Apply

Deploy Cloud Run services using the created service accounts:

```bash
# Lab 02
gcloud run deploy lab02-spring \
  --source ./l02-cloud-run-spring \
  --service-account $(terraform output -raw lab02_service_account)

# Lab 06
gcloud run deploy lab06-gcs \
  --source ./l06-gcs \
  --service-account $(terraform output -raw lab06_service_account) \
  --set-env-vars BUCKET_NAME=$(terraform output -raw bucket_name)

# Lab 07
gcloud run deploy lab07-pubsub \
  --source ./l07-pub-sub \
  --service-account $(terraform output -raw lab07_service_account) \
  --set-env-vars PROJECT_ID=YOUR_PROJECT_ID,PUBSUB_TOPIC=$(terraform output -raw pubsub_topic)
```

## Troubleshooting

**Error: "already exists"**
- Resource already created manually
- Import it: `terraform import google_storage_bucket.lab06 your-bucket-name`
- Or delete manually and re-run

**Error: "Permission denied"**
- Ensure you have Owner/Editor role on project
- Run: `gcloud auth application-default login`

**State file conflicts**
- Don't run Terraform from multiple terminals simultaneously
- Use remote state (GCS backend) for team collaboration
