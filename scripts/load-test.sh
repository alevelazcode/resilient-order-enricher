#!/bin/bash

# Load Testing Script for Resilient Order Enricher
# This script generates high volume order messages to test system performance

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default configuration
MESSAGES_COUNT=${1:-1000}
CONCURRENT_PRODUCERS=${2:-10}
BATCH_SIZE=${3:-100}
DELAY_MS=${4:-100}

# Kafka configuration
KAFKA_BOOTSTRAP_SERVERS=${KAFKA_BOOTSTRAP_SERVERS:-localhost:9092}
KAFKA_TOPIC=${KAFKA_TOPIC:-orders}

echo -e "${BLUE}🚀 Starting Load Test for Resilient Order Enricher${NC}"
echo "=============================================="
echo -e "${YELLOW}Configuration:${NC}"
echo "  Messages to send: $MESSAGES_COUNT"
echo "  Concurrent producers: $CONCURRENT_PRODUCERS"
echo "  Batch size: $BATCH_SIZE"
echo "  Delay between batches: ${DELAY_MS}ms"
echo "  Kafka servers: $KAFKA_BOOTSTRAP_SERVERS"
echo "  Topic: $KAFKA_TOPIC"
echo ""

# Function to check if Kafka is running
check_kafka() {
    echo -e "${BLUE}🔍 Checking Kafka availability...${NC}"
    if ! docker exec -it order-worker-kafka-1 kafka-topics --bootstrap-server localhost:9092 --list >/dev/null 2>&1; then
        echo -e "${RED}❌ Kafka is not running. Please start services with 'make start'${NC}"
        exit 1
    fi
    echo -e "${GREEN}✅ Kafka is running${NC}"
}

# Function to generate random order message
generate_order_message() {
    local order_id="order-$(date +%s%N | cut -b1-13)-$RANDOM"
    local customer_id="customer-$(shuf -n1 -e 456 123 789 101 202)"
    local product_count=$((RANDOM % 3 + 1))
    local products=""

    for ((i=1; i<=product_count; i++)); do
        local product_id="product-$(shuf -n1 -e 001 002 003 004 005)"
        local quantity=$((RANDOM % 5 + 1))

        if [ $i -eq 1 ]; then
            products="\"productId\":\"$product_id\",\"quantity\":$quantity"
        else
            products="$products},{\"productId\":\"$product_id\",\"quantity\":$quantity"
        fi
    done

    echo "{\"orderId\":\"$order_id\",\"customerId\":\"$customer_id\",\"products\":[{$products}]}"
}

# Function to send a batch of messages
send_batch() {
    local producer_id=$1
    local batch_num=$2
    local messages_in_batch=$3

    echo -e "${YELLOW}📦 Producer $producer_id sending batch $batch_num ($messages_in_batch messages)${NC}"

    for ((i=1; i<=messages_in_batch; i++)); do
        local message=$(generate_order_message)
        echo "$message" | docker exec -i order-worker-kafka-1 \
            kafka-console-producer \
            --bootstrap-server localhost:9092 \
            --topic "$KAFKA_TOPIC" >/dev/null 2>&1
    done

    echo -e "${GREEN}✅ Producer $producer_id completed batch $batch_num${NC}"
}

# Function to monitor system resources
monitor_system() {
    echo -e "${BLUE}📊 System Resource Monitoring${NC}"
    echo "CPU and Memory usage during load test:"

    # Monitor Java application
    local java_pid=$(docker exec order-worker-java-1 pgrep java 2>/dev/null || echo "")
    if [ -n "$java_pid" ]; then
        echo "Java Application Resource Usage:"
        docker exec order-worker-java-1 ps -p $java_pid -o pid,ppid,%cpu,%mem,cmd 2>/dev/null || echo "  Could not get Java process stats"
    fi

    # Monitor Docker containers
    echo ""
    echo "Docker Container Resource Usage:"
    docker stats --no-stream order-worker-java-1 order-worker-kafka-1 order-worker-mongo-1 order-worker-redis-1 2>/dev/null || echo "  Could not get container stats"
}

# Function to check message processing
check_processing() {
    echo -e "${BLUE}🔍 Checking message processing...${NC}"

    # Check Kafka consumer lag
    echo "Kafka Consumer Lag:"
    docker exec order-worker-kafka-1 \
        kafka-consumer-groups \
        --bootstrap-server localhost:9092 \
        --describe \
        --group order-worker-group 2>/dev/null || echo "  No consumer group found"

    # Check MongoDB order count
    echo ""
    echo "MongoDB Order Count:"
    local order_count=$(docker exec order-worker-mongo-1 \
        mongosh --quiet --eval "db = db.getSiblingDB('order_worker'); db.orders.countDocuments()" 2>/dev/null || echo "0")
    echo "  Total orders in MongoDB: $order_count"

    # Check Redis failed messages
    echo ""
    echo "Redis Failed Messages:"
    local failed_count=$(docker exec order-worker-redis-1 \
        redis-cli SCARD failed_messages_set 2>/dev/null || echo "0")
    echo "  Failed messages in Redis: $failed_count"
}

# Function to run performance test
run_load_test() {
    local start_time=$(date +%s)
    local messages_per_producer=$((MESSAGES_COUNT / CONCURRENT_PRODUCERS))
    local batches_per_producer=$((messages_per_producer / BATCH_SIZE))

    echo -e "${BLUE}⚡ Starting load test with $CONCURRENT_PRODUCERS concurrent producers${NC}"
    echo "Each producer will send $messages_per_producer messages in $batches_per_producer batches"
    echo ""

    # Start background processes for each producer
    local pids=()
    for ((producer=1; producer<=CONCURRENT_PRODUCERS; producer++)); do
        {
            for ((batch=1; batch<=batches_per_producer; batch++)); do
                send_batch $producer $batch $BATCH_SIZE
                sleep $(echo "scale=3; $DELAY_MS/1000" | bc) 2>/dev/null || sleep 1
            done
        } &
        pids+=($!)
    done

    # Wait for all producers to complete
    echo -e "${YELLOW}⏳ Waiting for all producers to complete...${NC}"
    for pid in "${pids[@]}"; do
        wait $pid
    done

    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    local messages_per_second=$((MESSAGES_COUNT / duration))

    echo ""
    echo -e "${GREEN}🎉 Load test completed!${NC}"
    echo "  Total messages sent: $MESSAGES_COUNT"
    echo "  Duration: ${duration}s"
    echo "  Throughput: ${messages_per_second} messages/second"
    echo ""
}

# Function to generate load test report
generate_report() {
    echo -e "${BLUE}📋 Load Test Report${NC}"
    echo "==================="

    # Get application logs for errors
    echo ""
    echo "Recent Application Logs (last 50 lines):"
    docker logs --tail 50 order-worker-java-1 2>/dev/null | grep -E "(ERROR|WARN)" || echo "  No errors found in recent logs"

    # Performance metrics
    echo ""
    check_processing

    echo ""
    monitor_system
}

# Main execution
main() {
    echo -e "${BLUE}🏁 Starting Load Test Execution${NC}"

    # Pre-flight checks
    check_kafka

    # Baseline measurements
    echo ""
    echo -e "${BLUE}📊 Baseline measurements:${NC}"
    check_processing

    # Run the load test
    echo ""
    run_load_test

    # Wait a bit for processing to complete
    echo -e "${YELLOW}⏳ Waiting 30 seconds for message processing to complete...${NC}"
    sleep 30

    # Generate final report
    echo ""
    generate_report

    echo ""
    echo -e "${GREEN}✅ Load test completed successfully!${NC}"
    echo ""
    echo -e "${BLUE}💡 Tips:${NC}"
    echo "  - Check application logs: make logs-java"
    echo "  - Monitor MongoDB: make check-mongo"
    echo "  - Check Redis: make check-redis"
    echo "  - View metrics: docker stats"
}

# Help function
show_help() {
    echo "Load Testing Script for Resilient Order Enricher"
    echo ""
    echo "Usage: $0 [MESSAGES_COUNT] [CONCURRENT_PRODUCERS] [BATCH_SIZE] [DELAY_MS]"
    echo ""
    echo "Arguments:"
    echo "  MESSAGES_COUNT        Total number of messages to send (default: 1000)"
    echo "  CONCURRENT_PRODUCERS  Number of concurrent producer processes (default: 10)"
    echo "  BATCH_SIZE           Messages per batch (default: 100)"
    echo "  DELAY_MS             Delay between batches in milliseconds (default: 100)"
    echo ""
    echo "Examples:"
    echo "  $0                    # Default: 1000 messages, 10 producers"
    echo "  $0 5000              # 5000 messages with default settings"
    echo "  $0 10000 20 50 50    # 10K messages, 20 producers, batches of 50, 50ms delay"
    echo ""
    echo "Environment Variables:"
    echo "  KAFKA_BOOTSTRAP_SERVERS  Kafka server address (default: localhost:9092)"
    echo "  KAFKA_TOPIC              Kafka topic name (default: orders)"
}

# Command line argument handling
if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    show_help
    exit 0
fi

# Run main function
main "$@"
