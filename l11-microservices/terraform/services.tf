# Order Service
resource "google_cloud_run_v2_service" "order_service" {
  name     = "order-service"
  location = var.region

  template {
    containers {
      image = "${var.region}-docker.pkg.dev/${var.project_id}/${var.artifact_registry_repo}/order-service:latest"

      env {
        name  = "INVENTORY_SERVICE_URL"
        value = google_cloud_run_v2_service.inventory_service.uri
      }

      env {
        name  = "NOTIFICATION_SERVICE_URL"
        value = google_cloud_run_v2_service.notification_service.uri
      }
    }

    service_account = google_service_account.order_service.email
  }

  depends_on = [
    google_project_service.run,
    google_cloud_run_v2_service.inventory_service,
    google_cloud_run_v2_service.notification_service
  ]
}

# Inventory Service
resource "google_cloud_run_v2_service" "inventory_service" {
  name     = "inventory-service"
  location = var.region

  template {
    containers {
      image = "${var.region}-docker.pkg.dev/${var.project_id}/${var.artifact_registry_repo}/inventory-service:latest"
    }

    service_account = google_service_account.inventory_service.email
  }

  depends_on = [google_project_service.run]
}

# Notification Service
resource "google_cloud_run_v2_service" "notification_service" {
  name     = "notification-service"
  location = var.region

  template {
    containers {
      image = "${var.region}-docker.pkg.dev/${var.project_id}/${var.artifact_registry_repo}/notification-service:latest"
    }

    service_account = google_service_account.notification_service.email
  }

  depends_on = [google_project_service.run]
}

# Service Accounts
resource "google_service_account" "order_service" {
  account_id   = "order-service-sa"
  display_name = "Order Service Account"
}

resource "google_service_account" "inventory_service" {
  account_id   = "inventory-service-sa"
  display_name = "Inventory Service Account"
}

resource "google_service_account" "notification_service" {
  account_id   = "notification-service-sa"
  display_name = "Notification Service Account"
}

# IAM: Allow order-service to invoke inventory-service
resource "google_cloud_run_service_iam_member" "order_to_inventory" {
  location = google_cloud_run_v2_service.inventory_service.location
  service  = google_cloud_run_v2_service.inventory_service.name
  role     = "roles/run.invoker"
  member   = "serviceAccount:${google_service_account.order_service.email}"
}

# IAM: Allow order-service to invoke notification-service
resource "google_cloud_run_service_iam_member" "order_to_notification" {
  location = google_cloud_run_v2_service.notification_service.location
  service  = google_cloud_run_v2_service.notification_service.name
  role     = "roles/run.invoker"
  member   = "serviceAccount:${google_service_account.order_service.email}"
}

# IAM: Allow public access to order-service (for testing)
resource "google_cloud_run_service_iam_member" "order_public" {
  location = google_cloud_run_v2_service.order_service.location
  service  = google_cloud_run_v2_service.order_service.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}

# IAM: Allow public access to inventory-service (for testing)
resource "google_cloud_run_service_iam_member" "inventory_public" {
  location = google_cloud_run_v2_service.inventory_service.location
  service  = google_cloud_run_v2_service.inventory_service.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}

# Firestore IAM
resource "google_project_iam_member" "order_firestore" {
  project = var.project_id
  role    = "roles/datastore.user"
  member  = "serviceAccount:${google_service_account.order_service.email}"
}

resource "google_project_iam_member" "inventory_firestore" {
  project = var.project_id
  role    = "roles/datastore.user"
  member  = "serviceAccount:${google_service_account.inventory_service.email}"
}

resource "google_project_iam_member" "notification_firestore" {
  project = var.project_id
  role    = "roles/datastore.user"
  member  = "serviceAccount:${google_service_account.notification_service.email}"
}
