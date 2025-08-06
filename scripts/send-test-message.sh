#!/bin/bash

# Script to send test messages to Kafka for order processing

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <order_id>"
    echo "Example: $0 order-123"
    exit 1
fi

ORDER_ID=$1

# Test message JSON
TEST_MESSAGE=$(cat <<EOF
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
)

echo "Sending test message for order: $ORDER_ID"
echo "Message: $TEST_MESSAGE"

# Send message to Kafka
echo "$TEST_MESSAGE" | docker exec -i $(docker ps -q -f name=kafka) kafka-console-producer --bootstrap-server localhost:9092 --topic orders

echo "Message sent successfully!"
echo "Check logs with: docker logs -f order-worker_order-worker_1"