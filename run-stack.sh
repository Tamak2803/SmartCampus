#!/bin/bash
set -e

echo "[STARTING RUN-STACK] Building package binaries..."
mvn clean package -DskipTests

echo "[DOCKER-COMPOSE] Launching broker infrastructure..."
docker compose down
docker compose up -d

echo "[WAITING] Waiting for RabbitMQ to initialize..."
sleep 10

echo "[STARTING SERVICES] Initializing microservices..."

# Start each service in the background, logging output locally
nohup java -jar student-service/target/student-service-1.0.0.jar > student-service.log 2>&1 &
echo "-> Student Profile Service running on port 8081"

nohup java -jar enrolment-service/target/enrolment-service-1.0.0.jar > enrolment-service.log 2>&1 &
echo "-> Course Enrolment Service running on port 8082"

nohup java -jar library-service/target/library-service-1.0.0.jar > library-service.log 2>&1 &
echo "-> Library Legacy Service running on port 8083"

nohup java -jar notification-service/target/notification-service-1.0.0.jar > notification-service.log 2>&1 &
echo "-> Notification Consumer running on port 8084"

nohup java -jar analytics-service/target/analytics-service-1.0.0.jar > analytics-service.log 2>&1 &
echo "-> Analytics Dashboard service running on port 8085"

echo "=================================================================="
echo "SmartCampus Connect is active. Run validation tests next."
echo "=================================================================="