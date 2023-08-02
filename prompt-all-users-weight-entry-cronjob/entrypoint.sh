#!/bin/bash
set -e

gcloud auth activate-service-account --key-file=/opt/service-account/key.json
gcloud pubsub topics publish --project=fitcentive-dev-03 prompt-all-users-weight-entry --message="{\"topic\":\"prompt-all-users-weight-entry\",\"payload\":{\"message\":\"Prompt EDT users to enter weight now\"},\"id\":\"a4e6dcaf-b562-4ce9-9249-c8ae63fcfce7\"}"

exec "$@"