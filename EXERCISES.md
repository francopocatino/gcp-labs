# Practice Exercises

Simple hands-on exercises to practice what you learned. Do these on your own time.

---

## Lab 02 - Cloud Run Basics

**Add a counter endpoint**

1. Add a static counter variable to `HelloController.java`
2. Create `/count` endpoint that increments and returns the count
3. Deploy and test
4. Restart the service → notice count resets (stateless!)

**Time:** 15 min
**What you learn:** Stateless services, local variables don't persist

---

## Lab 05 - Secrets & IAM

**Rotate a secret**

1. Update the secret value in Secret Manager:
   ```bash
   echo -n "new-secret-value" | gcloud secrets versions add lab02_api_key --data-file=-
   ```
2. Redeploy the service (picks up new version automatically)
3. Verify the new secret is being used
4. Disable the old secret version in console

**Time:** 10 min
**What you learn:** Secret rotation, versioning

---

## Lab 06 - Cloud Storage

**Add file download**

1. Upload a file to your bucket manually (console or gcloud)
2. Add `/gcs/download?name=file.txt` endpoint
3. Return file contents as response
4. Test with different file types (text, JSON, image)

**Time:** 20 min
**What you learn:** Reading files from GCS, content types

---

## Lab 07 - Pub/Sub

**Build a simple queue**

1. Publish 10 messages to your topic:
   ```bash
   for i in {1..10}; do
     curl -X POST "$SERVICE_URL/pubsub/publish" \
       -H "Content-Type: application/json" \
       -d "{\"data\":\"Message $i\"}"
   done
   ```
2. Check Cloud Console → Pub/Sub → Messages
3. See them queued up
4. Consume them (they disappear after ack)

**Time:** 15 min
**What you learn:** Message queuing, at-least-once delivery

---

## Lab 09 - Cloud SQL

**Add a category feature** (from earlier suggestion)

1. Add `category` field to `Item` entity
2. Add getter/setter
3. Add search endpoint:
   ```java
   @GetMapping("/items/search")
   public List<Item> searchByCategory(@RequestParam String category) {
     return itemRepository.findByCategory(category);
   }
   ```
4. Add repository method:
   ```java
   List<Item> findByCategory(String category);
   ```
5. Test creating items with categories and searching

**Time:** 30 min
**What you learn:** JPA queries, Spring Data magic

---

## Bonus: Terraform

**Destroy and recreate everything**

1. Run `terraform destroy` (deletes all infrastructure)
2. Wait for completion
3. Run `terraform apply` (recreates everything)
4. See how fast you can rebuild your entire setup

**Time:** 10 min
**What you learn:** Infrastructure as Code power, reproducibility

---

## Bonus: Break Things

**Practice debugging**

1. Remove IAM permission from service account
2. Deploy → see permission error
3. Fix it (add permission back)
4. Understand error messages

Or:
1. Change database password in Terraform
2. Apply → service can't connect
3. Update secret to match
4. Verify connection works

**Time:** 15-30 min
**What you learn:** Debugging, error messages, GCP permissions

---

## Tips

- Do one exercise at a time
- Actually type the code (don't copy-paste)
- Break things on purpose to see what happens
- Check Cloud Console to see changes
- Read error messages carefully

**Goal:** Practice, not perfection. Make mistakes and learn from them.
