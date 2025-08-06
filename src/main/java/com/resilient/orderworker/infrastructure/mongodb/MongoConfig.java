package com.resilient.orderworker.infrastructure.mongodb;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

/**
 * MongoDB configuration for reactive operations.
 */
@Configuration
public class MongoConfig extends AbstractReactiveMongoConfiguration {
    
    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017}")
    private String mongoUri;
    
    @Value("${spring.data.mongodb.database:order_worker}")
    private String databaseName;
    
    @Override
    protected String getDatabaseName() {
        return databaseName;
    }
    
    @Override
    @Bean
    public MongoClient reactiveMongoClient() {
        return MongoClients.create(mongoUri);
    }
    
    @Bean(name = "customReactiveMongoTemplate")
    public ReactiveMongoTemplate customReactiveMongoTemplate() {
        return new ReactiveMongoTemplate(reactiveMongoClient(), getDatabaseName());
    }
}