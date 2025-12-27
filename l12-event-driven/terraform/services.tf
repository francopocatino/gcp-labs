# User Service (Event Producer)
resource "google_cloud_run_v2_service" "user_service" {
  name     = "user-service"
  location = var.region

  template {
    containers {
      image = "${var.region}-docker.pkg.dev/${var.project_id}/${var.artifact_registry_repo}/user-service:latest"
    }
    service_account = google_service_account.user_service.email
  }

  depends_on = [google_project_service.run]
}

# Email Service (Consumer)
resource "google_cloud_run_v2_service" "email_service" {
  name     = "email-service"
  location = var.region

  template {
    containers {
      image = "${var.region}-docker.pkg.dev/${var.project_id}/${var.artifact_registry_repo}/email-service:latest"
    }
    service_account = google_service_account.email_service.email
  }

  depends_on = [google_project_service.run]
}

# Analytics Service (Consumer)
resource "google_cloud_run_v2_service" "analytics_service" {
  name     = "analytics-service"
  location = var.region

  template {
    containers {
      image = "${var.region}-docker.pkg.dev/${var.project_id}/${var.artifact_registry_repo}/analytics-service:latest"
    }
    service_account = google_service_account.analytics_service.email
  }

  depends_on = [google_project_service.run]
}

# CRM Service (Consumer)
resource "google_cloud_run_v2_service" "crm_service" {
  name     = "crm-service"
  location = var.region

  template {
    containers {
      image = "${var.region}-docker.pkg.dev/${var.project_id}/${var.artifact_registry_repo}/crm-service:latest"
    }
    service_account = google_service_account.crm_service.email
  }

  depends_on = [google_project_service.run]
}

# Service Accounts
resource "google_service_account" "user_service" {
  account_id   = "user-service-sa"
  display_name = "User Service Account"
}

resource "google_service_account" "email_service" {
  account_id   = "email-service-sa"
  display_name = "Email Service Account"
}

resource "google_service_account" "analytics_service" {
  account_id   = "analytics-service-sa"
  display_name = "Analytics Service Account"
}

resource "google_service_account" "crm_service" {
  account_id   = "crm-service-sa"
  display_name = "CRM Service Account"
}

# Pub/Sub service account (for push subscriptions)
resource "google_service_account" "pubsub_invoker" {
  account_id   = "pubsub-invoker-sa"
  display_name = "Pub/Sub Push Invoker"
}

# IAM: Allow public access to user-service (for testing)
resource "google_cloud_run_service_iam_member" "user_public" {
  location = google_cloud_run_v2_service.user_service.location
  service  = google_cloud_run_v2_service.user_service.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}

# IAM: Allow Pub/Sub to invoke consumer services
resource "google_cloud_run_service_iam_member" "email_pubsub_invoker" {
  location = google_cloud_run_v2_service.email_service.location
  service  = google_cloud_run_v2_service.email_service.name
  role     = "roles/run.invoker"
  member   = "serviceAccount:${google_service_account.pubsub_invoker.email}"
}

resource "google_cloud_run_service_iam_member" "analytics_pubsub_invoker" {
  location = google_cloud_run_v2_service.analytics_service.location
  service  = google_cloud_run_v2_service.analytics_service.name
  role     = "roles/run.invoker"
  member   = "serviceAccount:${google_service_account.pubsub_invoker.email}"
}

resource "google_cloud_run_service_iam_member" "crm_pubsub_invoker" {
  location = google_cloud_run_v2_service.crm_service.location
  service  = google_cloud_run_v2_service.crm_service.name
  role     = "roles/run.invoker"
  member   = "serviceAccount:${google_service_account.pubsub_invoker.email}"
}

# IAM: Pub/Sub publisher for user-service
resource "google_pubsub_topic_iam_member" "user_service_publisher" {
  topic  = google_pubsub_topic.user_events.name
  role   = "roles/pubsub.publisher"
  member = "serviceAccount:${google_service_account.user_service.email}"
}

# IAM: Firestore access for user-service
resource "google_project_iam_member" "user_firestore" {
  project = var.project_id
  role    = "roles/datastore.user"
  member  = "serviceAccount:${google_service_account.user_service.email}"
}

# Push Subscriptions
resource "google_pubsub_subscription" "email_subscription" {
  name  = "email-service-sub"
  topic = google_pubsub_topic.user_events.name

  push_config {
    push_endpoint = google_cloud_run_v2_service.email_service.uri

    oidc_token {
      service_account_email = google_service_account.pubsub_invoker.email
    }
  }

  ack_deadline_seconds = 20
  depends_on = [google_cloud_run_v2_service.email_service]
}

resource "google_pubsub_subscription" "analytics_subscription" {
  name  = "analytics-service-sub"
  topic = google_pubsub_topic.user_events.name

  push_config {
    push_endpoint = google_cloud_run_v2_service.analytics_service.uri

    oidc_token {
      service_account_email = google_service_account.pubsub_invoker.email
    }
  }

  ack_deadline_seconds = 20
  depends_on = [google_cloud_run_v2_service.analytics_service]
}

resource "google_pubsub_subscription" "crm_subscription" {
  name  = "crm-service-sub"
  topic = google_pubsub_topic.user_events.name

  push_config {
    push_endpoint = google_cloud_run_v2_service.crm_service.uri

    oidc_token {
      service_account_email = google_service_account.pubsub_invoker.email
    }
  }

  ack_deadline_seconds = 20
  depends_on = [google_cloud_run_v2_service.crm_service]
}
