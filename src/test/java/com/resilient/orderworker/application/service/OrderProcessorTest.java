/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.resilient.orderworker.application.command.ProcessOrderCommand;
import com.resilient.orderworker.application.port.out.CustomerProvider;
import com.resilient.orderworker.application.port.out.DistributedLock;
import com.resilient.orderworker.application.port.out.OrderRepository;
import com.resilient.orderworker.application.port.out.ProductProvider;
import com.resilient.orderworker.domain.customer.Customer;
import com.resilient.orderworker.domain.exception.OrderProcessingException;
import com.resilient.orderworker.domain.order.Order;
import com.resilient.orderworker.domain.product.Product;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class OrderProcessorTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CustomerProvider customerProvider;
    @Mock private ProductProvider productProvider;
    @Mock private DistributedLock distributedLock;

    private OrderProcessor processor;

    @BeforeEach
    void setUp() {
        processor =
                new OrderProcessor(
                        orderRepository, customerProvider, productProvider, distributedLock);
        when(distributedLock.executeWithLock(any(), any()))
                .thenAnswer(
                        inv -> {
                            Supplier<Mono<?>> supplier = inv.getArgument(1);
                            return supplier.get();
                        });
    }

    @Test
    void processOrder_savesNewOrder() {
        ProcessOrderCommand cmd =
                new ProcessOrderCommand("o1", "c1", List.of(new ProcessOrderCommand.Line("p1", 2)));
        Customer customer = new Customer("c1", "Alice", "ACTIVE");
        Product product = new Product("p1", "Laptop", "desc", new BigDecimal("999.99"));

        when(orderRepository.existsByOrderId("o1")).thenReturn(Mono.just(false));
        when(customerProvider.getCustomer("c1")).thenReturn(Mono.just(customer));
        when(productProvider.getProduct("p1")).thenReturn(Mono.just(product));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(processor.process(cmd))
                .assertNext(
                        order -> {
                            org.assertj.core.api.Assertions.assertThat(order.orderId())
                                    .isEqualTo("o1");
                            org.assertj.core.api.Assertions.assertThat(order.totalAmount())
                                    .isEqualByComparingTo(new BigDecimal("1999.98"));
                            org.assertj.core.api.Assertions.assertThat(order.lines()).hasSize(1);
                        })
                .verifyComplete();

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void processOrder_returnsExistingWhenAlreadyProcessed() {
        ProcessOrderCommand cmd =
                new ProcessOrderCommand("o1", "c1", List.of(new ProcessOrderCommand.Line("p1", 1)));
        Order existing =
                Order.create(
                        "o1",
                        "c1",
                        "Alice",
                        "ACTIVE",
                        List.of(
                                new com.resilient.orderworker.domain.order.OrderLine(
                                        "p1", "n", "d", BigDecimal.ONE, 1)));

        when(orderRepository.existsByOrderId("o1")).thenReturn(Mono.just(true));
        when(orderRepository.findByOrderId("o1")).thenReturn(Mono.just(existing));

        StepVerifier.create(processor.process(cmd)).expectNext(existing).verifyComplete();

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void processOrder_failsWhenCustomerInactive() {
        ProcessOrderCommand cmd =
                new ProcessOrderCommand("o1", "c1", List.of(new ProcessOrderCommand.Line("p1", 1)));
        Customer inactive = new Customer("c1", "Alice", "INACTIVE");
        Product product = new Product("p1", "L", "d", new BigDecimal("10"));

        when(orderRepository.existsByOrderId("o1")).thenReturn(Mono.just(false));
        when(customerProvider.getCustomer("c1")).thenReturn(Mono.just(inactive));
        when(productProvider.getProduct("p1")).thenReturn(Mono.just(product));

        StepVerifier.create(processor.process(cmd))
                .expectErrorMatches(
                        e ->
                                e instanceof OrderProcessingException
                                        && e.getMessage().contains("not active"))
                .verify();
    }

    @Test
    void processOrder_failsWhenProductInvalid() {
        ProcessOrderCommand cmd =
                new ProcessOrderCommand("o1", "c1", List.of(new ProcessOrderCommand.Line("p1", 1)));
        Customer customer = new Customer("c1", "Alice", "ACTIVE");
        Product invalid = new Product("p1", "", "d", new BigDecimal("10"));

        when(orderRepository.existsByOrderId(eq("o1"))).thenReturn(Mono.just(false));
        when(customerProvider.getCustomer("c1")).thenReturn(Mono.just(customer));
        when(productProvider.getProduct("p1")).thenReturn(Mono.just(invalid));

        StepVerifier.create(processor.process(cmd))
                .expectErrorMatches(
                        e ->
                                e instanceof OrderProcessingException
                                        && e.getMessage().contains("Invalid product"))
                .verify();
    }
}
