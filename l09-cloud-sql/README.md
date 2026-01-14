# Lab 09 - Cloud SQL

Connecting Cloud Run to a PostgreSQL database.

## What It Does

REST API with CRUD operations backed by Cloud SQL (managed PostgreSQL).

**Endpoints:**
- `POST /items` - Create item
- `GET /items` - List all items
- `GET /items/{id}` - Get single item
- `PUT /items/{id}` - Update item
- `DELETE /items/{id}` - Delete item

## Key Concepts

- Cloud SQL (managed database)
- Cloud SQL Proxy (secure connection without IP whitelisting)
- Spring Data JPA (ORM)
- Database migrations (Hibernate auto-ddl)

## Setup

### 1. Infrastructure (Terraform)

Cloud SQL instance already created via Terraform:
```bash
cd terraform
terraform apply
```

Creates:
- PostgreSQL instance (`lab09-postgres`)
- Database (`items_db`)
- User (`lab09user`)
- Service account with Cloud SQL Client role

### 2. Store Database Password in Secret Manager

```bash
echo -n "YOUR_DB_PASSWORD" | gcloud secrets create lab09_db_password --data-file=-
```

(Use the password from `terraform.tfvars`)

### 3. Deploy

```bash
gcloud run deploy lab09-sql \
  --source . \
  --region us-central1 \
  --service-account $(terraform output -raw lab09_service_account) \
  --set-env-vars DB_CONNECTION_NAME=$(terraform output -raw db_connection_name),DB_NAME=items_db,DB_USER=lab09user \
  --set-secrets DB_PASSWORD=lab09_db_password:latest \
  --allow-unauthenticated
```

## Testing

```bash
SERVICE_URL=$(gcloud run services describe lab09-sql --region us-central1 --format 'value(status.url)')

# Create an item
curl -X POST "$SERVICE_URL/items" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Item","description":"My first item"}'

# List all items
curl "$SERVICE_URL/items"

# Get specific item
curl "$SERVICE_URL/items/1"

# Update item
curl -X PUT "$SERVICE_URL/items/1" \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated Item","description":"Changed"}'

# Delete item
curl -X DELETE "$SERVICE_URL/items/1"
```

## How It Works

### Cloud SQL Proxy

Instead of whitelisting IP addresses, Cloud SQL Proxy uses:
1. Cloud Run service account authenticates via IAM (`roles/cloudsql.client`)
2. Connection string includes instance name: `jdbc:postgresql:///items_db?cloudSqlInstance=PROJECT:REGION:INSTANCE&socketFactory=...`
3. Socket factory creates secure tunnel

No public IP needed, no firewall rules.

### Database Connection

Application reads from env vars:
- `DB_CONNECTION_NAME` - Cloud SQL instance (project:region:instance)
- `DB_NAME` - Database name
- `DB_USER` - Username
- `DB_PASSWORD` - From Secret Manager (injected at runtime)

### JPA/Hibernate

`spring.jpa.hibernate.ddl-auto=update` automatically creates tables from `@Entity` classes. In production, use migrations (Flyway/Liquibase).

## Costs

**Cloud SQL db-f1-micro**: ~$7/month (smallest instance)

**Free Tier**: db-f1-micro qualifies for ~750 hours/month free tier (about 31 days). Pausing the instance when not in use preserves free tier quota.

**Pausing the instance**:
```bash
# In terraform/lab09-sql.tf, set:
activation_policy = "NEVER"

# Apply changes
cd terraform && terraform apply
```

To resume, change `activation_policy` back to `"ALWAYS"` and apply.

## Troubleshooting

### Container fails to start / Port timeout

**Symptom**: Deployment fails with "container failed to start and listen on port 8080"

**Common causes**:
1. **SQL instance is paused** (`activation_policy = "NEVER"`):
   - Resume instance: Set `activation_policy = "ALWAYS"` and run `terraform apply`
   - Wait 2-3 minutes for instance to start before deploying

2. **Database connection timeout**:
   - Check service account has `roles/cloudsql.client` role
   - Verify `DB_CONNECTION_NAME` matches instance connection name
   - Check database password secret exists: `gcloud secrets describe lab09_db_password`

3. **Application startup timeout**:
   - Cloud Run default timeout is 240 seconds
   - Check logs: `gcloud run services logs read lab09-sql --region us-central1`

### Health check fails

The `/health` endpoint checks database connectivity. If it returns 503:
- SQL instance may be paused or unreachable
- Connection pool exhausted (check max pool size settings)
- IAM permissions issue (verify service account roles)

### Local testing without Cloud SQL

Use H2 in-memory database for quick local testing:
```bash
# Add to application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

## Cleanup

```bash
# Delete Cloud Run service
gcloud run services delete lab09-sql --region us-central1

# Delete via Terraform (includes SQL instance)
cd terraform
terraform destroy
```

**Note**: Cloud SQL instances can take a few minutes to delete.

## Local Development

Run PostgreSQL locally with Docker:
```bash
docker run --name postgres -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres:15

# Update application.properties for local:
spring.datasource.url=jdbc:postgresql://localhost:5432/items_db
spring.datasource.username=postgres
spring.datasource.password=password
```

## Differences from Cloud Run Only

- **Stateful**: Data persists between deployments
- **Connection**: Uses Cloud SQL Proxy (not HTTP)
- **Cost**: Database always running (can't scale to zero)
- **Latency**: Small overhead for DB queries vs in-memory
