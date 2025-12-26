variable "project_id" {
  description = "GCP Project ID"
  type        = string
}

variable "region" {
  description = "GCP region"
  type        = string
  default     = "us-central1"
}

variable "github_repo" {
  description = "GitHub repository (format: owner/repo)"
  type        = string
}

# Lab 05 - Secrets
variable "secret_id" {
  description = "Secret Manager secret ID"
  type        = string
  default     = "lab02_api_key"
}

# Lab 06 - GCS
variable "bucket_name" {
  description = "Cloud Storage bucket name (must be globally unique)"
  type        = string
}

# Lab 07 - Pub/Sub
variable "pubsub_topic" {
  description = "Pub/Sub topic name"
  type        = string
  default     = "lab07-topic"
}

variable "pubsub_subscription" {
  description = "Pub/Sub subscription name"
  type        = string
  default     = "lab07-subscription"
}

# Lab 09 - Cloud SQL
variable "db_password" {
  description = "Cloud SQL database password"
  type        = string
  sensitive   = true
}
