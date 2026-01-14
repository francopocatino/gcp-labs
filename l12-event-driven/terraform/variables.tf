variable "project_id" {
  description = "GCP project ID"
  type        = string
}

variable "region" {
  description = "GCP region"
  type        = string
  default     = "us-central1"
}

variable "artifact_registry_repo" {
  description = "Artifact Registry repository name"
  type        = string
  default     = "cloud-run-apps"
}

variable "topic_name" {
  description = "Pub/Sub topic name"
  type        = string
  default     = "user-events"
}
