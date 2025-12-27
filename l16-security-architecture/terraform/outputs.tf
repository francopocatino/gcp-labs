output "security_policy_name" {
  value       = google_compute_security_policy.policy.name
  description = "Cloud Armor security policy name"
}

output "service_account_email" {
  value       = google_service_account.app.email
  description = "Application service account email"
}

output "secret_id" {
  value       = google_secret_manager_secret.db_password.secret_id
  description = "Secret Manager secret ID"
}

output "security_notes" {
  value = <<-EOT
    Security layers deployed:
    1. Cloud Armor policy: ${google_compute_security_policy.policy.name}
       - Rate limiting: 100 req/min
       - SQL injection protection
       - XSS protection

    2. Secret Manager: ${google_secret_manager_secret.db_password.secret_id}
       - Access granted to: ${google_service_account.app.email}

    3. Service Account: ${google_service_account.app.email}
       - Least privilege access

    Next steps:
    - Attach Cloud Armor policy to load balancer backend
    - Enable IAP for internal apps
    - Rotate secret: gcloud secrets versions add ${google_secret_manager_secret.db_password.secret_id} --data-file=-
    - Enable Security Command Center
  EOT
  description = "Security configuration summary"
}
