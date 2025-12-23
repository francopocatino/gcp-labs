# Lab 02 — Cloud Run + Spring App Sample

## Objective
Deploy a minimal Spring Boot web service to Google Cloud Run.

## Endpoints
- GET /        -> status payload
- GET /health  -> health check payload

## Local
```bash
cd app
mvn -DskipTests package
java -jar target/*.jar
# open http://localhost:8080/

