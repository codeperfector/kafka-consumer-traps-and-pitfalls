#!/bin/bash
docker-compose up -d zookeeper
sleep 5
docker-compose up -d kafka
keep_running=1
while [ $keep_running -eq 1 ]
do
  sleep 5
  # Is kafka running?
  kafka_service=`docker ps -q --filter label=com.docker.compose.service=kafka`
  if [ -z "$kafka_service" ]; # If empty
  then
    echo "Failed to start Kafka. Retrying"
    docker-compose start kafka
  else
    echo "Kafka started"
    keep_running=0
  fi
done
# Kafka is ready, start the other services
docker-compose up -d
echo "All services started"
