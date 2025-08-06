package com.resilient.orderworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Main application class for the Resilient Order Worker.
 * 
 * This application processes orders from Kafka, enriches them with external APIs,
 * and stores the processed data in MongoDB with resilience patterns.
 */
@SpringBootApplication
@EnableKafka
@EnableReactiveMongoRepositories
public class OrderWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderWorkerApplication.class, args);
    }
}