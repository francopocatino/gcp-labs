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
}

# Enable APIs
resource "google_project_service" "run" {
  service            = "run.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "compute" {
  service            = "compute.googleapis.com"
  disable_on_destroy = false
}

# Cloud Run services in multiple regions
resource "google_cloud_run_v2_service" "app_us" {
  name     = "app-service"
  location = "us-central1"

  template {
    containers {
      image = "gcr.io/cloudrun/hello"
      env {
        name  = "REGION"
        value = "us-central1"
      }
    }
  }

  depends_on = [google_project_service.run]
}

resource "google_cloud_run_v2_service" "app_eu" {
  name     = "app-service"
  location = "europe-west1"

  template {
    containers {
      image = "gcr.io/cloudrun/hello"
      env {
        name  = "REGION"
        value = "europe-west1"
      }
    }
  }

  depends_on = [google_project_service.run]
}

resource "google_cloud_run_v2_service" "app_asia" {
  name     = "app-service"
  location = "asia-east1"

  template {
    containers {
      image = "gcr.io/cloudrun/hello"
      env {
        name  = "REGION"
        value = "asia-east1"
      }
    }
  }

  depends_on = [google_project_service.run]
}

# IAM: Allow public access
resource "google_cloud_run_service_iam_member" "public_us" {
  location = google_cloud_run_v2_service.app_us.location
  service  = google_cloud_run_v2_service.app_us.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}

resource "google_cloud_run_service_iam_member" "public_eu" {
  location = google_cloud_run_v2_service.app_eu.location
  service  = google_cloud_run_v2_service.app_eu.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}

resource "google_cloud_run_service_iam_member" "public_asia" {
  location = google_cloud_run_v2_service.app_asia.location
  service  = google_cloud_run_v2_service.app_asia.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}

# Serverless NEGs (Network Endpoint Groups)
resource "google_compute_region_network_endpoint_group" "neg_us" {
  name                  = "app-neg-us"
  network_endpoint_type = "SERVERLESS"
  region                = "us-central1"

  cloud_run {
    service = google_cloud_run_v2_service.app_us.name
  }

  depends_on = [google_project_service.compute]
}

resource "google_compute_region_network_endpoint_group" "neg_eu" {
  name                  = "app-neg-eu"
  network_endpoint_type = "SERVERLESS"
  region                = "europe-west1"

  cloud_run {
    service = google_cloud_run_v2_service.app_eu.name
  }
}

resource "google_compute_region_network_endpoint_group" "neg_asia" {
  name                  = "app-neg-asia"
  network_endpoint_type = "SERVERLESS"
  region                = "asia-east1"

  cloud_run {
    service = google_cloud_run_v2_service.app_asia.name
  }
}

# Backend service
resource "google_compute_backend_service" "default" {
  name                  = "app-backend"
  protocol              = "HTTP"
  port_name             = "http"
  timeout_sec           = 30
  enable_cdn            = false
  load_balancing_scheme = "EXTERNAL"

  backend {
    group = google_compute_region_network_endpoint_group.neg_us.id
  }

  backend {
    group = google_compute_region_network_endpoint_group.neg_eu.id
  }

  backend {
    group = google_compute_region_network_endpoint_group.neg_asia.id
  }

  health_checks = [google_compute_health_check.default.id]
}

# Health check
resource "google_compute_health_check" "default" {
  name               = "app-health-check"
  check_interval_sec = 10
  timeout_sec        = 5

  http_health_check {
    port         = 443
    request_path = "/"
  }
}

# URL map
resource "google_compute_url_map" "default" {
  name            = "app-url-map"
  default_service = google_compute_backend_service.default.id
}

# HTTP proxy
resource "google_compute_target_http_proxy" "default" {
  name    = "app-http-proxy"
  url_map = google_compute_url_map.default.id
}

# Global forwarding rule (load balancer frontend)
resource "google_compute_global_forwarding_rule" "default" {
  name       = "app-forwarding-rule"
  target     = google_compute_target_http_proxy.default.id
  port_range = "80"
}
