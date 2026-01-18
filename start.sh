#!/bin/bash

docker-compose up --build -d
cd src/main/resources/db/changelog
chmod +x run_migrations.sh
./run_migrations.sh "jdbc:postgresql://localhost:5432/my-market-db" "postgres" "postgres" "org.postgresql.Driver"
