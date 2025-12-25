# Troubleshooting

Common issues I ran into and how I fixed them.

## Costs (Important!)

**Good news**: Mostly free if you're just learning.

### Free Tier (as of 2024)

- **Cloud Run**: 2 million requests/month free
- **Cloud Storage**: 5GB storage, 5GB egress free
- **Pub/Sub**: 10GB messages/month free
- **Secret Manager**: 6 active secrets free
- **Cloud Build**: 120 build-minutes/day free

**My usage**: Testing these labs costs basically $0. Just don't leave services running idle or forget to delete resources.

### Cleanup to Avoid Charges

```bash
# Option 1: Delete everything with Terraform
cd terraform
terraform destroy

# Option 2: Delete manually
gcloud run services delete lab02-spring --region us-central1 --quiet
gcloud run services delete lab06-gcs --region us-central1 --quiet
gcloud run services delete lab07-pubsub --region us-central1 --quiet
gcloud storage rm -r gs://YOUR-BUCKET-NAME
gcloud pubsub topics delete lab07-topic
gcloud secrets delete lab02_api_key
```

**Tip**: Cloud Run scales to zero when not used, so no charges for idle time.

---

## Common Errors

### 1. "Permission denied" on Artifact Registry

**Error**:
```
Permission 'artifactregistry.repositories.get' denied
```

**Fix**: GitHub deployer service account needs more permissions. Added in Terraform:
```hcl
roles/artifactregistry.writer
roles/cloudbuild.builds.builder
roles/serviceusage.serviceUsageConsumer
```

Run `terraform apply` to add them.

---

### 2. Workload Identity Pool "already exists"

**Error**:
```
Error 409: Requested entity already exists
```

**Why**: Deleted pools have 30-day soft-delete period.

**Fix**: Use a different name (`github-pool-v2` instead of `github-pool`).

---

### 3. "Application Default Credentials not found"

**Error**:
```
could not find default credentials
```

**Fix**:
```bash
gcloud auth application-default login
```

This is for Terraform and local testing. Different from `gcloud auth login` (which is for CLI commands).

---

### 4. Tests fail with 500 error

**Why**: Tests try to connect to real GCS/Pub/Sub without credentials.

**Fix**: Keep tests simple - just check endpoints respond, don't mock GCP services. Real integration tests should run in CI with actual permissions.

---

### 5. GitHub Actions fails on first push

**Why**: Terraform creates infrastructure, but workflows reference it. Need to:
1. Run `terraform apply` first
2. Then push code

**Order**:
1. Setup Terraform
2. Apply infrastructure
3. Push code → triggers workflows

---

## Useful Commands

### Check what's running
```bash
gcloud run services list
gcloud storage buckets list
gcloud pubsub topics list
gcloud secrets list
```

### View logs
```bash
gcloud run services logs read lab02-spring --limit 50
```

### See current costs
```bash
gcloud billing accounts list
# Then check billing reports in console
```

### Reset a deployment
```bash
# Delete service and redeploy from scratch
gcloud run services delete SERVICE_NAME
git push  # triggers redeploy
```

---

## Tips

- Use `--quiet` flag to skip confirmations in scripts
- Service accounts are project-specific (can't share between projects)
- Cloud Run builds can be slow first time (caches dependencies after)
- Check GitHub Actions logs for detailed error messages
- Terraform state is local (don't delete `terraform.tfstate`)

---

## If Something Breaks

1. Check GitHub Actions logs (most detailed errors)
2. Check Cloud Run logs: `gcloud run services logs read SERVICE_NAME`
3. Verify IAM permissions in console
4. Try `terraform plan` to see what changed
5. Delete and recreate (it's all in code anyway)

---

## Learning Resources

I used:
- GCP official docs (most accurate)
- Cloud Run quickstarts
- Terraform Google provider docs
- GitHub Actions docs for OIDC

Don't overthink it - just build, break, fix, repeat.
