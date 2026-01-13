#!/bin/bash

URL="$1"
USERNAME="$2"
PASSWORD="$3"
DRIVER="$4"

liquibase \
  --changeLogFile=./db.changelog-master.yaml \
  --url="${URL}" \
  --username="${USERNAME}" \
  --password="${PASSWORD}" \
  --driver="${DRIVER}" \
  update
