# Lab 07 — Pub/Sub (Event-Driven Architecture)

## Goal
Build an event-driven flow using Pub/Sub and Cloud Run with push subscriptions.

## Core Concepts
- Topic / Subscription
- Producer / Consumer
- Push delivery
- At-least-once delivery
- Automatic retries
- Least Privilege IAM

## Security
- Dedicated Service Account
- roles/pubsub.publisher on topic
- roles/run.invoker on Cloud Run

## Behavior
- HTTP 2xx = ACK
- HTTP error = retry
- Messages may be delivered more than once

## Notes
- Consumers must be idempotent
- Do not rely on ordering unless explicitly configured
