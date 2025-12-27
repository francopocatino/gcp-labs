output "user_service_url" {
  value       = google_cloud_run_v2_service.user_service.uri
  description = "User Service URL"
}

output "topic_name" {
  value       = google_pubsub_topic.user_events.name
  description = "Pub/Sub topic name"
}

output "test_command" {
  value = <<-EOT
    Test the event-driven flow:

    export USER_SERVICE_URL=${google_cloud_run_v2_service.user_service.uri}

    curl -X POST $USER_SERVICE_URL/users \
      -H "Content-Type: application/json" \
      -d '{"email": "test@example.com", "name": "Test User"}'

    Check logs:
    gcloud logging read "resource.labels.service_name=email-service" --limit 10
    gcloud logging read "resource.labels.service_name=analytics-service" --limit 10
    gcloud logging read "resource.labels.service_name=crm-service" --limit 10
  EOT
  description = "Command to test event-driven architecture"
}
