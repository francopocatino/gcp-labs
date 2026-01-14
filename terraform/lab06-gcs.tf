# Lab 06 - Cloud Storage + IAM

# GCS bucket
resource "google_storage_bucket" "lab06" {
  name          = var.bucket_name
  location      = var.region
  force_destroy = true # WARNING: Deletes all objects when bucket is destroyed

  uniform_bucket_level_access = true
}

# Service account for lab06
resource "google_service_account" "lab06" {
  account_id   = "sa-lab06-gcs"
  display_name = "Service Account for lab06-gcs"
}

# Grant service account access to bucket (least privilege)
resource "google_storage_bucket_iam_member" "lab06_object_user" {
  bucket = google_storage_bucket.lab06.name
  role   = "roles/storage.objectUser"
  member = "serviceAccount:${google_service_account.lab06.email}"
}
