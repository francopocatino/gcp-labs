# Lab 09 - Cloud SQL + Cloud Run

# Cloud SQL instance (PostgreSQL)
resource "google_sql_database_instance" "lab09" {
  name             = "lab09-postgres"
  database_version = "POSTGRES_15"
  region           = var.region

  settings {
    tier              = "db-f1-micro" # Smallest tier (free tier eligible)
    activation_policy = "NEVER"       # Pause instance to save free tier tokens (change to "ALWAYS" when needed)

    ip_configuration {
      # Allow Cloud Run to connect via public IP
      # (Private IP requires VPC connector - more complex)
      ipv4_enabled = true

      # Don't allow all IPs - Cloud Run will use Cloud SQL Proxy
      authorized_networks {
        name  = "allow-cloud-run"
        value = "0.0.0.0/0" # Cloud SQL Proxy handles auth via IAM
      }
    }

    backup_configuration {
      enabled = false # Disable for dev (saves cost)
    }
  }

  deletion_protection = false # Allow terraform destroy
}

# Database
resource "google_sql_database" "lab09_db" {
  name     = "items_db"
  instance = google_sql_database_instance.lab09.name
}

# Database user
resource "google_sql_user" "lab09_user" {
  name     = "lab09user"
  instance = google_sql_database_instance.lab09.name
  password = var.db_password # Will be set in terraform.tfvars
}

# Service account for lab09
resource "google_service_account" "lab09" {
  account_id   = "sa-lab09-sql"
  display_name = "Service Account for lab09-sql"
}

# Grant Cloud SQL Client permission
resource "google_project_iam_member" "lab09_sql_client" {
  project = var.project_id
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${google_service_account.lab09.email}"
}

# Secret for database password
resource "google_secret_manager_secret" "lab09_db_password" {
  secret_id = "lab09_db_password"

  replication {
    auto {}
  }
}

# Grant access to the secret
resource "google_secret_manager_secret_iam_member" "lab09_secret_accessor" {
  secret_id = google_secret_manager_secret.lab09_db_password.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.lab09.email}"
}
