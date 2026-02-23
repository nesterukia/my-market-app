#!/bin/bash

CHANGE_LOG_FILE="$1"
URL="$2"
USERNAME="$3"
PASSWORD="$4"
DRIVER="$5"

liquibase \
  --changeLogFile="${CHANGE_LOG_FILE}" \
  --url="${URL}" \
  --username="${USERNAME}" \
  --password="${PASSWORD}" \
  --driver="${DRIVER}" \
  update
