# Outputs - display created resources

# Lab 05 - Secrets
output "secret_id" {
  description = "Secret Manager secret ID"
  value       = google_secret_manager_secret.lab02_api_key.secret_id
}

output "lab02_service_account" {
  description = "Lab02 service account email"
  value       = google_service_account.lab02.email
}

# Lab 06 - GCS
output "bucket_name" {
  description = "GCS bucket name"
  value       = google_storage_bucket.lab06.name
}

output "bucket_url" {
  description = "GCS bucket URL"
  value       = google_storage_bucket.lab06.url
}

output "lab06_service_account" {
  description = "Lab06 service account email"
  value       = google_service_account.lab06.email
}

# Lab 07 - Pub/Sub
output "pubsub_topic" {
  description = "Pub/Sub topic name"
  value       = google_pubsub_topic.lab07.name
}

output "lab07_service_account" {
  description = "Lab07 service account email"
  value       = google_service_account.lab07.email
}

# Lab 08 - CI/CD
output "workload_identity_provider" {
  description = "Workload Identity Provider resource name (use in GitHub Actions)"
  value       = google_iam_workload_identity_pool_provider.github.name
}

output "github_service_account" {
  description = "GitHub Actions service account email"
  value       = google_service_account.github_deployer.email
}

# Lab 09 - Cloud SQL
output "db_instance_name" {
  description = "Cloud SQL instance name"
  value       = google_sql_database_instance.lab09.name
}

output "db_connection_name" {
  description = "Cloud SQL connection name (for Cloud SQL Proxy)"
  value       = google_sql_database_instance.lab09.connection_name
}

output "lab09_service_account" {
  description = "Lab09 service account email"
  value       = google_service_account.lab09.email
}

# Helpful commands
output "next_steps" {
  description = "Next steps"
  value       = <<-EOT

  Resources created successfully!

  To deploy Cloud Run services with these resources:

  Lab 02:
    gcloud run deploy lab02-spring --source ./l02-cloud-run-spring --service-account ${google_service_account.lab02.email}

  Lab 06:
    gcloud run deploy lab06-gcs --source ./l06-gcs --service-account ${google_service_account.lab06.email} --set-env-vars BUCKET_NAME=${google_storage_bucket.lab06.name}

  Lab 07:
    gcloud run deploy lab07-pubsub --source ./l07-pub-sub --service-account ${google_service_account.lab07.email} --set-env-vars PROJECT_ID=${var.project_id},PUBSUB_TOPIC=${google_pubsub_topic.lab07.name}

  Update secret value:
    echo -n "your-secret-value" | gcloud secrets versions add ${google_secret_manager_secret.lab02_api_key.secret_id} --data-file=-

  EOT
}
