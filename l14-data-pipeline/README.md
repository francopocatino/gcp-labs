# Lab 14 - Data Pipeline Architecture

Move and transform data from sources to destinations. Batch and streaming patterns.

## Architecture

**Batch Pipeline:**
```
GCS (CSV upload) → Cloud Storage trigger → Cloud Run → BigQuery
```

**Streaming Pipeline:**
```
Application → Pub/Sub → Cloud Run → BigQuery
```

**Pattern:**
- Extract data from source
- Transform (clean, enrich, aggregate)
- Load to destination
- Idempotent processing

**Stack:**
- Cloud Storage (data lake)
- Pub/Sub (streaming)
- Cloud Run (processing)
- BigQuery (data warehouse)

## Batch vs Streaming

| Aspect | Batch | Streaming |
|--------|-------|-----------|
| Trigger | Scheduled/manual | Real-time events |
| Latency | Minutes to hours | Seconds |
| Data Volume | Large files | Individual records |
| Cost | Lower (scheduled) | Higher (always on) |
| Use Case | Reports, analytics | Real-time dashboards |

## Use Cases

**When to use:**
- Move data between systems
- Generate reports/analytics
- Real-time dashboards
- Data transformation/enrichment
- Aggregate metrics

**Batch:**
- Daily reports
- End-of-day processing
- Large file imports
- Historical analysis

**Streaming:**
- Real-time dashboards
- Alerting
- Fraud detection
- Live metrics

**Tradeoffs:**
- Pro: automated data flow, scalable, decoupled
- Con: complexity, eventual consistency, monitoring overhead

## Setup

```bash
# Enable APIs
gcloud services enable bigquery.googleapis.com storage.googleapis.com pubsub.googleapis.com

# Create BigQuery dataset
bq mk --dataset sales_data

# Create bucket
gsutil mb gs://PROJECT_ID-data-pipeline

# Deploy
cd l14-data-pipeline/terraform
terraform init
terraform apply
```

## Commands

**Batch Pipeline:**
```bash
# Upload CSV to trigger processing
gsutil cp sample-data.csv gs://PROJECT_ID-data-pipeline/uploads/

# Data is automatically processed and loaded to BigQuery

# Query results
bq query --use_legacy_sql=false \
  'SELECT * FROM sales_data.transactions LIMIT 10'
```

**Streaming Pipeline:**
```bash
# Publish event
gcloud pubsub topics publish sales-events \
  --message '{"order_id": "123", "amount": 99.99, "customer_id": "456"}'

# Event is processed and loaded to BigQuery in real-time

# Query
bq query --use_legacy_sql=false \
  'SELECT COUNT(*) FROM sales_data.realtime_transactions'
```

## Sample Data Schema

```json
{
  "order_id": "string",
  "customer_id": "string",
  "product_id": "string",
  "amount": "float",
  "timestamp": "timestamp"
}
```

## Processing Logic

**Transform steps:**
1. Validate data (required fields, data types)
2. Enrich (add derived fields, lookup data)
3. Clean (remove duplicates, handle nulls)
4. Aggregate (if needed)
5. Load to BigQuery

**Error handling:**
- Invalid records → separate error table
- Retry transient failures
- Dead letter queue for persistent failures
- Idempotent processing (dedupe by order_id)

## Notes

- Cloud Storage triggers have ~10s latency
- Pub/Sub guarantees at-least-once delivery
- BigQuery streaming inserts cost $0.01/200MB
- Use partitioned tables for large datasets
- Enable BigQuery streaming buffer monitoring
- Consider Dataflow for complex transformations

Cost:
- Storage: ~$0.02/GB/month
- BigQuery: $5/TB queried
- Streaming inserts: $0.01/200MB
- Cloud Run: ~$0 (within free tier)
