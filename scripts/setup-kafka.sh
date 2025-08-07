#!/bin/bash

# Script to setup Kafka topics for the order worker application

echo "Setting up Kafka topics..."

# Wait for Kafka to be ready
echo "Waiting for Kafka to be ready..."
sleep 30

# Create orders topic
docker exec -it $(docker ps -q -f name=kafka) kafka-topics --bootstrap-server localhost:9092 --create --topic orders --partitions 3 --replication-factor 1 --if-not-exists

# Create dead letter queue topic for failed messages
docker exec -it $(docker ps -q -f name=kafka) kafka-topics --bootstrap-server localhost:9092 --create --topic orders-dlq --partitions 3 --replication-factor 1 --if-not-exists

# List topics to verify
echo "Created topics:"
docker exec -it $(docker ps -q -f name=kafka) kafka-topics --bootstrap-server localhost:9092 --list

echo "Kafka setup complete!"
