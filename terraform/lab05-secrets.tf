# Lab 05 - Secret Manager + IAM

# Secret in Secret Manager
resource "google_secret_manager_secret" "lab02_api_key" {
  secret_id = var.secret_id

  replication {
    auto {}
  }
}

# Secret version with dummy value (update manually or via separate process)
resource "google_secret_manager_secret_version" "lab02_api_key_v1" {
  secret      = google_secret_manager_secret.lab02_api_key.id
  secret_data = "change-me-after-apply"
}

# Service account for lab02
resource "google_service_account" "lab02" {
  account_id   = "sa-lab02-spring"
  display_name = "Service Account for lab02-spring"
}

# Grant service account access to the secret
resource "google_secret_manager_secret_iam_member" "lab02_accessor" {
  secret_id = google_secret_manager_secret.lab02_api_key.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.lab02.email}"
}
