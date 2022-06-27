#!/bin/bash
set -e

gcloud auth activate-service-account --key-file=/opt/service-account/key.json
gcloud pubsub topics publish --project=fitcentive-1210 clear-username-lock-table --message="{\"topic\":\"clear-username-lock-table\",\"payload\":{\"message\":\"Clear username lock table now\"},\"id\":\"df3bd32e-05e1-4d33-843b-44261a828222\"}"

exec "$@"