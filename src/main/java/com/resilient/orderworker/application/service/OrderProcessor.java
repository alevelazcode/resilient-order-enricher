/*
 * Copyright (c) 2025 Resilient Order Enricher
 *
 * Licensed under the MIT License.
 */
package com.resilient.orderworker.application.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.resilient.orderworker.application.command.ProcessOrderCommand;
import com.resilient.orderworker.application.port.in.ProcessOrderUseCase;
import com.resilient.orderworker.application.port.out.CustomerProvider;
import com.resilient.orderworker.application.port.out.DistributedLock;
import com.resilient.orderworker.application.port.out.OrderRepository;
import com.resilient.orderworker.application.port.out.ProductProvider;
import com.resilient.orderworker.domain.customer.Customer;
import com.resilient.orderworker.domain.exception.OrderProcessingException;
import com.resilient.orderworker.domain.order.Order;
import com.resilient.orderworker.domain.order.OrderLine;
import com.resilient.orderworker.domain.product.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderProcessor implements ProcessOrderUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(OrderProcessor.class);
    private static final int ENRICHMENT_CONCURRENCY = 8;

    private final OrderRepository orderRepository;
    private final CustomerProvider customerProvider;
    private final ProductProvider productProvider;
    private final DistributedLock distributedLock;

    public OrderProcessor(
            OrderRepository orderRepository,
            CustomerProvider customerProvider,
            ProductProvider productProvider,
            DistributedLock distributedLock) {
        this.orderRepository = orderRepository;
        this.customerProvider = customerProvider;
        this.productProvider = productProvider;
        this.distributedLock = distributedLock;
    }

    @Override
    public Mono<Order> process(ProcessOrderCommand command) {
        return distributedLock.executeWithLock(
                "order-lock:" + command.orderId(), () -> processIdempotent(command));
    }

    private Mono<Order> processIdempotent(ProcessOrderCommand command) {
        return orderRepository
                .existsByOrderId(command.orderId())
                .flatMap(
                        exists -> {
                            if (exists) {
                                LOG.info("Order {} already processed, skipping", command.orderId());
                                return orderRepository.findByOrderId(command.orderId());
                            }
                            return enrich(command);
                        });
    }

    private Mono<Order> enrich(ProcessOrderCommand command) {
        Mono<Customer> customerMono = customerProvider.getCustomer(command.customerId());

        List<String> productIds =
                command.lines().stream()
                        .map(ProcessOrderCommand.Line::productId)
                        .distinct()
                        .toList();

        Mono<List<Product>> productsMono =
                Flux.fromIterable(productIds)
                        .flatMap(productProvider::getProduct, ENRICHMENT_CONCURRENCY)
                        .collectList();

        return Mono.zip(customerMono, productsMono)
                .flatMap(tuple -> buildAndSave(command, tuple.getT1(), tuple.getT2()))
                .doOnSuccess(order -> LOG.info("Order {} processed", command.orderId()))
                .doOnError(
                        err -> LOG.warn("Order {} failed: {}", command.orderId(), err.toString()));
    }

    private Mono<Order> buildAndSave(
            ProcessOrderCommand command, Customer customer, List<Product> products) {
        if (!customer.isActive()) {
            return Mono.error(
                    new OrderProcessingException(
                            "Customer is not active: " + customer.customerId()));
        }

        Map<String, Product> productById =
                products.stream()
                        .collect(Collectors.toMap(Product::productId, Function.identity()));

        for (ProcessOrderCommand.Line line : command.lines()) {
            Product p = productById.get(line.productId());
            if (p == null) {
                return Mono.error(
                        new OrderProcessingException("Product not found: " + line.productId()));
            }
            if (!p.isValid()) {
                return Mono.error(
                        new OrderProcessingException("Invalid product: " + line.productId()));
            }
        }

        List<OrderLine> orderLines =
                command.lines().stream()
                        .map(
                                line -> {
                                    Product p = productById.get(line.productId());
                                    return new OrderLine(
                                            p.productId(),
                                            p.name(),
                                            p.description(),
                                            p.price(),
                                            line.quantity());
                                })
                        .toList();

        Order order =
                Order.create(
                        command.orderId(),
                        customer.customerId(),
                        customer.name(),
                        customer.status(),
                        orderLines);

        return orderRepository.save(order);
    }
}
