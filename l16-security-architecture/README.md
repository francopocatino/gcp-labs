# Lab 16 - Security Architecture

Defense in depth. Multiple security layers to protect applications.

## Architecture

```
Internet
    ↓
Cloud Armor (WAF, DDoS protection)
    ↓
Load Balancer
    ↓
Identity-Aware Proxy (IAP)
    ↓
Cloud Run Service
    ↓
VPC Service Controls
    ↓
Secret Manager / KMS
```

**Layers:**
1. Network: Cloud Armor, firewall rules
2. Application: IAP, authentication
3. Data: encryption, Secret Manager
4. IAM: least privilege, service accounts
5. Monitoring: Security Command Center, Cloud Logging

**Stack:**
- Cloud Armor (WAF)
- Identity-Aware Proxy
- VPC Service Controls
- Secret Manager
- Cloud KMS

## Security Layers

| Layer | Technology | Protection |
|-------|------------|------------|
| Network | Cloud Armor | DDoS, SQL injection, XSS |
| Identity | IAP | Authentication, authorization |
| Application | Cloud Run IAM | Service-level access |
| Data | Secret Manager | Credentials, keys |
| Encryption | Cloud KMS | Data encryption keys |
| Monitoring | Security Command Center | Threats, vulnerabilities |

## Use Cases

**When to use:**
- Public-facing applications
- Sensitive data handling
- Compliance requirements (PCI-DSS, HIPAA)
- Enterprise applications
- Multi-tenant systems

**Security Patterns:**
- Defense in depth (multiple layers)
- Zero trust (verify everything)
- Least privilege (minimal permissions)
- Secrets rotation
- Audit logging

**Tradeoffs:**
- Pro: strong security, compliance, audit trails
- Con: complexity, cost, potential latency

## Setup

```bash
# Enable APIs
gcloud services enable \
  compute.googleapis.com \
  iap.googleapis.com \
  secretmanager.googleapis.com \
  cloudkms.googleapis.com

# Deploy
cd l16-security-architecture/terraform
terraform init
terraform apply
```

## Cloud Armor Rules

```bash
# Block specific countries
gcloud compute security-policies rules create 1000 \
  --security-policy my-policy \
  --expression "origin.region_code == 'CN'" \
  --action "deny-403"

# Rate limiting
gcloud compute security-policies rules create 2000 \
  --security-policy my-policy \
  --expression "true" \
  --action "rate-based-ban" \
  --rate-limit-threshold-count 100 \
  --rate-limit-threshold-interval-sec 60

# SQL injection protection
gcloud compute security-policies rules create 3000 \
  --security-policy my-policy \
  --expression "evaluatePreconfiguredExpr('sqli-stable')" \
  --action "deny-403"
```

## IAP Configuration

```bash
# Enable IAP
gcloud iap web enable --resource-type backend-services \
  --service my-backend

# Grant access to specific users
gcloud iap web add-iam-policy-binding --resource-type backend-services \
  --service my-backend \
  --member user:alice@example.com \
  --role roles/iap.httpsResourceAccessor
```

## Secret Manager

```bash
# Create secret
echo -n "my-database-password" | \
  gcloud secrets create db-password --data-file=-

# Grant access to service account
gcloud secrets add-iam-policy-binding db-password \
  --member serviceAccount:my-app@PROJECT.iam.gserviceaccount.com \
  --role roles/secretmanager.secretAccessor

# Access from code
gcloud secrets versions access latest --secret="db-password"
```

## Security Best Practices

**Network:**
- Use Cloud Armor for public services
- Enable DDoS protection
- Restrict ingress to known IPs (if possible)
- Use private Google Access for internal services

**Identity:**
- Use IAP for internal apps
- Implement OAuth 2.0 / OIDC
- MFA for admin access
- Service accounts for applications

**Data:**
- Encrypt at rest (default in GCP)
- Encrypt in transit (TLS/HTTPS)
- Use Secret Manager for credentials
- Rotate secrets regularly

**IAM:**
- Principle of least privilege
- Use service accounts (not user accounts)
- Regular access reviews
- Separate dev/staging/prod permissions

**Monitoring:**
- Enable Cloud Audit Logs
- Monitor failed auth attempts
- Alert on anomalies
- Regular security scans

## Cost Estimates

- Cloud Armor: $5/policy + $1/1M requests
- IAP: Free
- Secret Manager: $0.06/10K access operations
- Cloud KMS: $0.03/10K operations
- Security Command Center: Free (standard tier)

## Notes

- Cloud Armor rules evaluated in priority order (1000, 2000, 3000...)
- IAP requires HTTPS load balancer
- Secrets can have multiple versions
- KMS keys are regional
- Use VPC Service Controls for data exfiltration protection
- Regular penetration testing recommended

Cost: ~$5-20/month (depends on traffic)
