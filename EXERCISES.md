# Practice Exercises

Things to try on your own to actually practice this stuff.

---

## Lab 02 - Cloud Run

Add a counter endpoint that increments each time you hit it. Something like `/count` that returns a number. Deploy it and test. Then restart the service and notice the counter resets because Cloud Run is stateless.

Learning goal: understand stateless services

---

## Lab 05 - Secrets

Practice rotating a secret:
```bash
echo -n "new-value" | gcloud secrets versions add lab02_api_key --data-file=-
```

Redeploy the service and verify it picks up the new value. Then go to Secret Manager console and disable the old version.

Learning goal: secret rotation

---

## Lab 06 - Cloud Storage

Upload a file to your bucket (console or gcloud). Add a download endpoint that reads and returns file contents. Try different file types to see how they're handled.

Learning goal: reading files from GCS

---

## Lab 07 - Pub/Sub

Send 10 messages to your topic in a loop. Check the Pub/Sub console to see them queued. Then consume them and watch them disappear.

Learning goal: message queuing

---

## Lab 09 - Cloud SQL

Add a `category` field to items. Let users filter items by category. Requires updating the entity, adding a repository method (`findByCategory`), and creating a search endpoint.

Learning goal: database queries with JPA

---

## Terraform

Run `terraform destroy` to delete everything. Then `terraform apply` to recreate it all. See how fast you can rebuild your entire infrastructure.

Learning goal: infrastructure as code reproducibility

---

## Break Things

Remove an IAM permission and watch the deployment fail. Then fix it. Or change the database password and see the connection error. Breaking things intentionally helps you understand error messages.

Learning goal: debugging in production
