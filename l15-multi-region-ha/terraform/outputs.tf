output "load_balancer_ip" {
  value       = google_compute_global_forwarding_rule.default.ip_address
  description = "Global load balancer IP address"
}

output "regions" {
  value = [
    google_cloud_run_v2_service.app_us.location,
    google_cloud_run_v2_service.app_eu.location,
    google_cloud_run_v2_service.app_asia.location
  ]
  description = "Deployed regions"
}

output "test_commands" {
  value = <<-EOT
    Test the load balancer:
    curl http://${google_compute_global_forwarding_rule.default.ip_address}/

    Check backend health:
    gcloud compute backend-services get-health ${google_compute_backend_service.default.name} --global

    Simulate failure (set min instances to 0):
    gcloud run services update app-service --region us-central1 --min-instances 0
  EOT
  description = "Commands to test multi-region setup"
}
