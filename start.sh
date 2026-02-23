#!/bin/bash

echo "Loading .env..."
set -a
source .env
set +a

docker-compose up --build -d

chmod +x ./postgres-db/run_migrations.sh

./postgres-db/run_migrations.sh "./market-app/src/main/resources/db/changelog/db.changelog-master.yaml" \
  "jdbc:postgresql://localhost:5432/$MARKET_APP_POSTGRES_DB" \
  "$MARKET_APP_POSTGRES_USER" \
  "$MARKET_APP_POSTGRES_PASSWORD" \
  "org.postgresql.Driver"

./postgres-db/run_migrations.sh "./payment-service/src/main/resources/db/changelog/db.changelog-master.yaml" \
  "jdbc:postgresql://localhost:5432/$PAYMENT_SERVICE_POSTGRES_DB" \
  "$PAYMENT_SERVICE_POSTGRES_USER" \
  "$PAYMENT_SERVICE_POSTGRES_PASSWORD" \
  "org.postgresql.Driver"
