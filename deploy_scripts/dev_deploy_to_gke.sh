#!/bin/bash

kubectl delete -n user deployment/user-service

# Delete old 1.0 image from gcr
echo "y" | gcloud container images delete gcr.io/fitcentive-dev-03/user:1.0 --force-delete-tags

# Build and push image to gcr
sbt docker:publish

kubectl apply -f deployment/gke-dev-env/

cd clear-username-lock-cronjob && \
  docker build -t gcr.io/fitcentive-dev-03/gcloud-user-cron-pubsub-image:latest -t gcr.io/fitcentive-dev-03/gcloud-user-cron-pubsub-image:1.0 . && \
  docker push gcr.io/fitcentive-dev-03/gcloud-user-cron-pubsub-image:1.0

cd .. && cd prompt-all-users-weight-entry-cronjob && \
  docker build -t gcr.io/fitcentive-dev-03/gcloud-user-cron-02-pubsub-image:latest -t gcr.io/fitcentive-dev-03/gcloud-user-cron-02-pubsub-image:1.0 . && \
  docker push gcr.io/fitcentive-dev-03/gcloud-user-cron-02-pubsub-image:1.0

cd .. && cd prompt-all-users-diary-entry-cronjob && \
docker build -t gcr.io/fitcentive-dev-03/gcloud-user-cron-03-pubsub-image:latest -t gcr.io/fitcentive-dev-03/gcloud-user-cron-03-pubsub-image:1.0 . && \
docker push gcr.io/fitcentive-dev-03/gcloud-user-cron-03-pubsub-image:1.0