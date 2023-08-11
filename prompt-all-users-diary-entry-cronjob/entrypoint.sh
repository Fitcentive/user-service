#!/bin/bash
set -e

gcloud auth activate-service-account --key-file=/opt/service-account/key.json
gcloud pubsub topics publish --project=fitcentive-dev-03 prompt-all-users-diary-entry --message="{\"topic\":\"prompt-all-users-diary-entry\",\"payload\":{\"message\":\"Prompt EDT users to enter diary now\"},\"id\":\"d8d84df0-0a07-4141-bd73-d7bb9af6be5e\"}"

exec "$@"