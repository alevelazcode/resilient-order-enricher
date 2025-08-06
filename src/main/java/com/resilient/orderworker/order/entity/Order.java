package com.resilient.orderworker.order.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB entity representing a processed order.
 * Contains enriched data from external APIs.
 */
@Document(collection = "orders")
public class Order {
    
    @Id
    private String id;
    
    @Field("orderId")
    private String orderId;
    
    @Field("customerId")
    private String customerId;
    
    @Field("customerName")
    private String customerName;
    
    @Field("customerStatus")
    private String customerStatus;
    
    @Field("products")
    private List<OrderProduct> products;
    
    @Field("totalAmount")
    private Double totalAmount;
    
    @Field("processedAt")
    private LocalDateTime processedAt;
    
    @Field("status")
    private OrderStatus status;
    
    // Constructors
    public Order() {}
    
    public Order(String orderId, String customerId, String customerName, 
                 String customerStatus, List<OrderProduct> products, 
                 Double totalAmount, OrderStatus status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerStatus = customerStatus;
        this.products = products;
        this.totalAmount = totalAmount;
        this.status = status;
        this.processedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getCustomerStatus() {
        return customerStatus;
    }
    
    public void setCustomerStatus(String customerStatus) {
        this.customerStatus = customerStatus;
    }
    
    public List<OrderProduct> getProducts() {
        return products;
    }
    
    public void setProducts(List<OrderProduct> products) {
        this.products = products;
    }
    
    public Double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    /**
     * Nested class representing a product within an order.
     */
    public static class OrderProduct {
        @Field("productId")
        private String productId;
        
        @Field("name")
        private String name;
        
        @Field("description")
        private String description;
        
        @Field("price")
        private Double price;
        
        @Field("quantity")
        private Integer quantity;
        
        @Field("subtotal")
        private Double subtotal;
        
        // Constructors
        public OrderProduct() {}
        
        public OrderProduct(String productId, String name, String description, 
                           Double price, Integer quantity) {
            this.productId = productId;
            this.name = name;
            this.description = description;
            this.price = price;
            this.quantity = quantity;
            this.subtotal = price * quantity;
        }
        
        // Getters and Setters
        public String getProductId() {
            return productId;
        }
        
        public void setProductId(String productId) {
            this.productId = productId;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public Double getPrice() {
            return price;
        }
        
        public void setPrice(Double price) {
            this.price = price;
        }
        
        public Integer getQuantity() {
            return quantity;
        }
        
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
        
        public Double getSubtotal() {
            return subtotal;
        }
        
        public void setSubtotal(Double subtotal) {
            this.subtotal = subtotal;
        }
    }
    
    /**
     * Enum representing the status of an order.
     */
    public enum OrderStatus {
        PROCESSED, FAILED, RETRY
    }
}