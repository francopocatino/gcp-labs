output "bucket_name" {
  value       = google_storage_bucket.data_pipeline.name
  description = "Data pipeline bucket name"
}

output "bigquery_dataset" {
  value       = google_bigquery_dataset.sales_data.dataset_id
  description = "BigQuery dataset ID"
}

output "pubsub_topic" {
  value       = google_pubsub_topic.sales_events.name
  description = "Pub/Sub topic name"
}

output "test_commands" {
  value = <<-EOT
    Test batch pipeline:
    echo "order_id,customer_id,product_id,amount,timestamp" > sample.csv
    echo "001,c123,p456,99.99,2024-01-01T10:00:00Z" >> sample.csv
    gsutil cp sample.csv gs://${google_storage_bucket.data_pipeline.name}/uploads/

    Test streaming pipeline:
    gcloud pubsub topics publish ${google_pubsub_topic.sales_events.name} \
      --message '{"order_id":"002","customer_id":"c124","product_id":"p457","amount":149.99,"timestamp":"2024-01-01T11:00:00Z"}'

    Query results:
    bq query --use_legacy_sql=false 'SELECT * FROM sales_data.transactions LIMIT 10'
    bq query --use_legacy_sql=false 'SELECT * FROM sales_data.realtime_transactions LIMIT 10'
  EOT
  description = "Commands to test data pipelines"
}
