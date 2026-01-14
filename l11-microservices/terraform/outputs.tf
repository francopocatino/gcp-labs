output "order_service_url" {
  value       = google_cloud_run_v2_service.order_service.uri
  description = "Order Service URL"
}

output "inventory_service_url" {
  value       = google_cloud_run_v2_service.inventory_service.uri
  description = "Inventory Service URL"
}

output "notification_service_url" {
  value       = google_cloud_run_v2_service.notification_service.uri
  description = "Notification Service URL"
}

output "deploy_commands" {
  value = <<-EOT
    Build and push Docker images:

    cd ../order-service
    gcloud builds submit --tag ${var.region}-docker.pkg.dev/${var.project_id}/${var.artifact_registry_repo}/order-service:latest

    cd ../inventory-service
    gcloud builds submit --tag ${var.region}-docker.pkg.dev/${var.project_id}/${var.artifact_registry_repo}/inventory-service:latest

    cd ../notification-service
    gcloud builds submit --tag ${var.region}-docker.pkg.dev/${var.project_id}/${var.artifact_registry_repo}/notification-service:latest
  EOT
  description = "Commands to build and deploy services"
}
