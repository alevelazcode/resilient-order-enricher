#!/usr/bin/env bash
# Publish a single order message to the `orders` Kafka topic via the running container.

set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <order_id>" >&2
  echo "Example: $0 order-123" >&2
  exit 1
fi

ORDER_ID=$1
BROKER=${KAFKA_BROKER:-kafka:9092}
TOPIC=${KAFKA_TOPIC:-orders}

container=$(docker compose ps -q kafka)
if [[ -z "$container" ]]; then
  echo "Kafka container is not running. Start it with: docker compose up -d kafka" >&2
  exit 1
fi

read -r -d '' PAYLOAD <<EOF || true
{
  "orderId": "${ORDER_ID}",
  "customerId": "customer-456",
  "products": [
    {
      "productId": "product-789",
      "quantity": 2
    }
  ]
}
EOF

echo "Publishing to topic '$TOPIC' on broker '$BROKER':"
echo "$PAYLOAD"

echo "$PAYLOAD" | docker exec -i "$container" \
  /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server "$BROKER" --topic "$TOPIC"

echo "Sent."
