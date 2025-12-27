terraform {
  required_version = ">= 1.0"
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
}

# Enable APIs
resource "google_project_service" "bigquery" {
  service            = "bigquery.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "storage" {
  service            = "storage.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "pubsub" {
  service            = "pubsub.googleapis.com"
  disable_on_destroy = false
}

# BigQuery dataset
resource "google_bigquery_dataset" "sales_data" {
  dataset_id = "sales_data"
  location   = var.region

  depends_on = [google_project_service.bigquery]
}

# BigQuery table for batch data
resource "google_bigquery_table" "transactions" {
  dataset_id = google_bigquery_dataset.sales_data.dataset_id
  table_id   = "transactions"

  schema = <<EOF
[
  {"name": "order_id", "type": "STRING", "mode": "REQUIRED"},
  {"name": "customer_id", "type": "STRING", "mode": "REQUIRED"},
  {"name": "product_id", "type": "STRING", "mode": "REQUIRED"},
  {"name": "amount", "type": "FLOAT64", "mode": "REQUIRED"},
  {"name": "timestamp", "type": "TIMESTAMP", "mode": "REQUIRED"}
]
EOF
}

# BigQuery table for streaming data
resource "google_bigquery_table" "realtime_transactions" {
  dataset_id = google_bigquery_dataset.sales_data.dataset_id
  table_id   = "realtime_transactions"

  schema = <<EOF
[
  {"name": "order_id", "type": "STRING", "mode": "REQUIRED"},
  {"name": "customer_id", "type": "STRING", "mode": "REQUIRED"},
  {"name": "product_id", "type": "STRING", "mode": "REQUIRED"},
  {"name": "amount", "type": "FLOAT64", "mode": "REQUIRED"},
  {"name": "timestamp", "type": "TIMESTAMP", "mode": "REQUIRED"}
]
EOF
}

# Cloud Storage bucket for batch uploads
resource "google_storage_bucket" "data_pipeline" {
  name     = "${var.project_id}-data-pipeline"
  location = var.region

  uniform_bucket_level_access = true

  depends_on = [google_project_service.storage]
}

# Pub/Sub topic for streaming events
resource "google_pubsub_topic" "sales_events" {
  name = "sales-events"

  depends_on = [google_project_service.pubsub]
}
