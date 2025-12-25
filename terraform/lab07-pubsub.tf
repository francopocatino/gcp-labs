# Lab 07 - Pub/Sub + IAM

# Pub/Sub topic
resource "google_pubsub_topic" "lab07" {
  name = var.pubsub_topic
}

# Service account for lab07
resource "google_service_account" "lab07" {
  account_id   = "sa-lab07-pubsub"
  display_name = "Service Account for lab07-pubsub"
}

# Grant service account permission to publish to topic
resource "google_pubsub_topic_iam_member" "lab07_publisher" {
  topic  = google_pubsub_topic.lab07.name
  role   = "roles/pubsub.publisher"
  member = "serviceAccount:${google_service_account.lab07.email}"
}

# Push subscription (requires Cloud Run service URL - set manually or use data source)
# Uncomment and update after deploying lab07 Cloud Run service
#
# resource "google_pubsub_subscription" "lab07" {
#   name  = var.pubsub_subscription
#   topic = google_pubsub_topic.lab07.name
#
#   push_config {
#     push_endpoint = "https://YOUR_CLOUD_RUN_URL/pubsub/consume"
#   }
#
#   ack_deadline_seconds = 60
# }
