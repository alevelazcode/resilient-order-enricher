package com.resilient.orderworker.order.service;

import com.resilient.orderworker.customer.dto.CustomerResponse;
import com.resilient.orderworker.customer.service.CustomerService;
import com.resilient.orderworker.infrastructure.redis.DistributedLockService;
import com.resilient.orderworker.order.dto.OrderMessage;
import com.resilient.orderworker.order.entity.Order;
import com.resilient.orderworker.order.repository.OrderRepository;
import com.resilient.orderworker.product.dto.ProductResponse;
import com.resilient.orderworker.product.service.ProductService;
import com.resilient.orderworker.common.exception.OrderProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderProcessingService.
 */
@ExtendWith(MockitoExtension.class)
class OrderProcessingServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private CustomerService customerService;
    
    @Mock
    private ProductService productService;
    
    @Mock
    private DistributedLockService lockService;
    
    private OrderProcessingService orderProcessingService;
    
    @BeforeEach
    void setUp() {
        orderProcessingService = new OrderProcessingService(
            orderRepository, customerService, productService, lockService);
    }
    
    @Test
    void shouldProcessOrderSuccessfully() {
        // Given
        OrderMessage orderMessage = createOrderMessage();
        CustomerResponse customer = new CustomerResponse("customer-1", "John Doe", "ACTIVE");
        ProductResponse product = new ProductResponse("product-1", "Laptop", "Gaming laptop", 999.0);
        Order savedOrder = createExpectedOrder();
        
        when(lockService.executeWithLock(eq("order-1"), any(Supplier.class)))
            .thenAnswer(invocation -> {
                Supplier<Mono<Order>> supplier = invocation.getArgument(1);
                return supplier.get();
            });
        
        when(orderRepository.existsByOrderId("order-1")).thenReturn(Mono.just(false));
        when(customerService.getCustomer("customer-1")).thenReturn(Mono.just(customer));
        when(productService.getProducts(List.of("product-1"))).thenReturn(Flux.just(product));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(savedOrder));
        
        // When & Then
        StepVerifier.create(orderProcessingService.processOrder(orderMessage))
            .expectNext(savedOrder)
            .verifyComplete();
        
        verify(orderRepository).save(any(Order.class));
    }
    
    @Test
    void shouldReturnExistingOrderWhenAlreadyProcessed() {
        // Given
        OrderMessage orderMessage = createOrderMessage();
        Order existingOrder = createExpectedOrder();
        
        when(lockService.executeWithLock(eq("order-1"), any(Supplier.class)))
            .thenAnswer(invocation -> {
                Supplier<Mono<Order>> supplier = invocation.getArgument(1);
                return supplier.get();
            });
        
        when(orderRepository.existsByOrderId("order-1")).thenReturn(Mono.just(true));
        when(orderRepository.findByOrderId("order-1")).thenReturn(Mono.just(existingOrder));
        
        // When & Then
        StepVerifier.create(orderProcessingService.processOrder(orderMessage))
            .expectNext(existingOrder)
            .verifyComplete();
        
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void shouldFailWhenCustomerIsNotActive() {
        // Given
        OrderMessage orderMessage = createOrderMessage();
        CustomerResponse inactiveCustomer = new CustomerResponse("customer-1", "John Doe", "INACTIVE");
        ProductResponse product = new ProductResponse("product-1", "Laptop", "Gaming laptop", 999.0);
        
        when(lockService.executeWithLock(eq("order-1"), any(Supplier.class)))
            .thenAnswer(invocation -> {
                Supplier<Mono<Order>> supplier = invocation.getArgument(1);
                return supplier.get();
            });
        
        when(orderRepository.existsByOrderId("order-1")).thenReturn(Mono.just(false));
        when(customerService.getCustomer("customer-1")).thenReturn(Mono.just(inactiveCustomer));
        when(productService.getProducts(List.of("product-1"))).thenReturn(Flux.just(product));
        
        // When & Then
        StepVerifier.create(orderProcessingService.processOrder(orderMessage))
            .expectErrorMatches(throwable -> 
                throwable instanceof OrderProcessingException &&
                throwable.getMessage().contains("Customer is not active"))
            .verify();
    }
    
    @Test
    void shouldFailWhenProductIsInvalid() {
        // Given
        OrderMessage orderMessage = createOrderMessage();
        CustomerResponse customer = new CustomerResponse("customer-1", "John Doe", "ACTIVE");
        ProductResponse invalidProduct = new ProductResponse("product-1", "", "Gaming laptop", 999.0);
        
        when(lockService.executeWithLock(eq("order-1"), any(Supplier.class)))
            .thenAnswer(invocation -> {
                Supplier<Mono<Order>> supplier = invocation.getArgument(1);
                return supplier.get();
            });
        
        when(orderRepository.existsByOrderId("order-1")).thenReturn(Mono.just(false));
        when(customerService.getCustomer("customer-1")).thenReturn(Mono.just(customer));
        when(productService.getProducts(List.of("product-1"))).thenReturn(Flux.just(invalidProduct));
        
        // When & Then
        StepVerifier.create(orderProcessingService.processOrder(orderMessage))
            .expectErrorMatches(throwable -> 
                throwable instanceof OrderProcessingException &&
                throwable.getMessage().contains("One or more products are invalid"))
            .verify();
    }
    
    private OrderMessage createOrderMessage() {
        return new OrderMessage(
            "order-1",
            "customer-1",
            List.of(new OrderMessage.ProductInfo("product-1", 2))
        );
    }
    
    private Order createExpectedOrder() {
        Order order = new Order();
        order.setOrderId("order-1");
        order.setCustomerId("customer-1");
        order.setCustomerName("John Doe");
        order.setCustomerStatus("ACTIVE");
        order.setTotalAmount(1998.0);
        order.setStatus(Order.OrderStatus.PROCESSED);
        return order;
    }
}