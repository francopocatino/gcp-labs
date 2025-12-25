# Lab 04 — Configuration via Environment Variables

## Objective
Update app config without rebuilding the container. Understand Cloud Run revisions.

## Key Concepts
- **Immutable images**: Container can't change once built
- **Mutable config**: Env vars can change anytime
- **Revisions**: Each config change = new revision (snapshot of code + config)

## Update Config

```bash
export REGION=us-central1
export SERVICE=lab02-spring

# Change env vars without rebuilding
gcloud run services update ${SERVICE} \
  --region ${REGION} \
  --set-env-vars MESSAGE="Updated config, no rebuild needed"

# Test it
SERVICE_URL=$(gcloud run services describe ${SERVICE} --region ${REGION} --format 'value(status.url)')
curl ${SERVICE_URL}/
```

New revision created automatically, traffic switches to it.

## View Revisions

```bash
# List all revisions
gcloud run revisions list --service ${SERVICE} --region ${REGION}

# See details
gcloud run revisions describe REVISION_NAME --region ${REGION}
```

Each revision has a name like `lab02-spring-00003-abc`.

## Rollback

If new config breaks something:
```bash
# Find the working revision
gcloud run revisions list --service ${SERVICE} --region ${REGION}

# Route traffic back to it
gcloud run services update-traffic ${SERVICE} \
  --region ${REGION} \
  --to-revisions REVISION_NAME=100
```

Instant rollback, no redeploy needed.

## Traffic Splitting

Can do gradual rollouts:
```bash
# 80% new, 20% old (canary deployment)
gcloud run services update-traffic ${SERVICE} \
  --region ${REGION} \
  --to-revisions NEW_REVISION=80,OLD_REVISION=20
```

Useful for testing changes with subset of traffic.

## Why This Matters
- Same image works in dev/staging/prod (different env vars)
- Fast config updates (seconds, not minutes)
- Easy rollbacks
- No rebuilds for config-only changes

## Notes
- Revision created on every: config change, code deploy, secret update
- Old revisions stick around (can manually delete)
- View in Console: Cloud Run → service → Revisions tab
- Revisions are immutable (can't edit, only create new)

## Next
[Lab 05](../l05-secrets-iam/) - Secret Manager for sensitive config
