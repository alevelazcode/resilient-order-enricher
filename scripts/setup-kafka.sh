#!/usr/bin/env bash
# Create the Kafka topics the worker expects. Topics auto-create at first publish
# (KAFKA_AUTO_CREATE_TOPICS_ENABLE=true), so this script is only needed when you
# want explicit configuration (e.g. partitions, replication).

set -euo pipefail

KAFKA_BIN=/opt/kafka/bin
BROKER=${KAFKA_BROKER:-kafka:9092}
PARTITIONS=${KAFKA_PARTITIONS:-3}
REPLICATION=${KAFKA_REPLICATION:-1}

container=$(docker compose ps -q kafka)
if [[ -z "$container" ]]; then
  echo "Kafka container is not running. Start it with: docker compose up -d kafka" >&2
  exit 1
fi

create_topic() {
  local topic=$1
  docker exec "$container" "$KAFKA_BIN/kafka-topics.sh" \
    --bootstrap-server "$BROKER" \
    --create --if-not-exists \
    --topic "$topic" \
    --partitions "$PARTITIONS" \
    --replication-factor "$REPLICATION"
}

create_topic orders
create_topic orders-dlq

echo "Topics in cluster:"
docker exec "$container" "$KAFKA_BIN/kafka-topics.sh" --bootstrap-server "$BROKER" --list
