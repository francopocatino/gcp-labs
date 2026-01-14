# Lab 15 - Multi-Region High Availability

Deploy across multiple regions for zero downtime and disaster recovery.

## Architecture

```
Users (global)
    ↓
Global Load Balancer
    ├─→ us-central1 (Cloud Run)
    ├─→ europe-west1 (Cloud Run)
    └─→ asia-east1 (Cloud Run)
```

**Pattern:**
- Application deployed in multiple regions
- Global load balancer distributes traffic
- Health checks detect failures
- Automatic failover
- Active-active (all regions serve traffic)

**Stack:**
- Cloud Run (multi-region)
- Global Load Balancer
- Cloud CDN (optional)
- Health checks

## Deployment Patterns

| Pattern | Behavior | Use Case |
|---------|----------|----------|
| Active-Active | All regions serve traffic | Max availability, global users |
| Active-Passive | Standby region only if primary fails | Cost optimization |
| Regional | Single region only | Simple, low cost |

## Use Cases

**When to use:**
- Global user base
- Zero downtime requirements
- Disaster recovery needed
- Regulatory compliance (data residency)
- SLA > 99.9%

**When NOT to use:**
- Regional user base only
- Cost constraints
- Simple application
- No HA requirements

**Tradeoffs:**
- Pro: high availability, low latency globally, disaster recovery
- Con: higher cost, data consistency complexity, increased ops

## Setup

```bash
# Deploy
cd l15-multi-region-ha/terraform
terraform init
terraform apply

# Note: Creates Cloud Run services in 3 regions + global LB
```

## Commands

```bash
# Get load balancer IP
export LB_IP=$(terraform output -raw load_balancer_ip)

# Test from different locations
curl http://$LB_IP/health
curl http://$LB_IP/region

# Simulate region failure (update service to 0 instances)
gcloud run services update my-service --region us-central1 --min-instances 0

# Traffic automatically routes to healthy regions
curl http://$LB_IP/region

# Check backend health
gcloud compute backend-services get-health my-backend-service --global
```

## Health Checks

```yaml
Health Check Configuration:
- Path: /health
- Interval: 10s
- Timeout: 5s
- Healthy threshold: 2
- Unhealthy threshold: 3
```

## Failover Behavior

1. Health check fails in us-central1
2. Load balancer marks backend unhealthy
3. Traffic routes to europe-west1 and asia-east1
4. When us-central1 recovers, traffic resumes
5. No manual intervention required

## Cost Estimates

- Cloud Run (3 regions): ~$0-10/month (depends on traffic)
- Load Balancer: ~$18/month base + $0.008/GB
- Total: ~$20-50/month for HA setup

## Notes

- Use Cloud CDN for static assets
- Enable connection draining (30s default)
- Set appropriate health check intervals
- Monitor failover events
- Test disaster recovery regularly
- Consider data replication strategy (Firestore, Cloud SQL replicas)
- Use session affinity if needed

## SLA Targets

- Single region: 99.95%
- Multi-region active-active: 99.99%
- With proper design: 99.999% possible

Cost: ~$20-50/month (not free tier)
