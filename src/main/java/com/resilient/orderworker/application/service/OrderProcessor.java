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

import com.resilient.orderworker.application.command.ProcessOrderCommand;
import com.resilient.orderworker.application.port.in.ProcessOrderUseCase;
import com.resilient.orderworker.application.port.out.CustomerProvider;
import com.resilient.orderworker.application.port.out.DistributedLock;
import com.resilient.orderworker.application.port.out.LockKey;
import com.resilient.orderworker.application.port.out.OrderRepository;
import com.resilient.orderworker.application.port.out.ProductProvider;
import com.resilient.orderworker.domain.customer.Customer;
import com.resilient.orderworker.domain.order.Order;
import com.resilient.orderworker.domain.order.OrderAssembler;
import com.resilient.orderworker.domain.order.OrderValidator;
import com.resilient.orderworker.domain.product.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Application service. Pure POJO — no Spring annotations. Wired into the context by {@link
 * com.resilient.orderworker.infrastructure.config.ApplicationServicesConfig}. Its sole job is
 * orchestration: acquire the lock, ensure idempotency, fan-out enrichment, delegate validation and
 * assembly to the domain services, persist.
 */
public class OrderProcessor implements ProcessOrderUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(OrderProcessor.class);
    private static final int ENRICHMENT_CONCURRENCY = 8;

    private final OrderRepository orderRepository;
    private final CustomerProvider customerProvider;
    private final ProductProvider productProvider;
    private final DistributedLock distributedLock;
    private final OrderValidator validator;
    private final OrderAssembler assembler;

    public OrderProcessor(
            OrderRepository orderRepository,
            CustomerProvider customerProvider,
            ProductProvider productProvider,
            DistributedLock distributedLock,
            OrderValidator validator,
            OrderAssembler assembler) {
        this.orderRepository = orderRepository;
        this.customerProvider = customerProvider;
        this.productProvider = productProvider;
        this.distributedLock = distributedLock;
        this.validator = validator;
        this.assembler = assembler;
    }

    @Override
    public Mono<Order> process(ProcessOrderCommand command) {
        return distributedLock.executeWithLock(
                LockKey.forOrder(command.orderId()), () -> processIdempotent(command));
    }

    private Mono<Order> processIdempotent(ProcessOrderCommand command) {
        return orderRepository
                .existsByOrderId(command.orderId())
                .flatMap(exists -> exists ? loadExisting(command) : enrichAndPersist(command));
    }

    private Mono<Order> loadExisting(ProcessOrderCommand command) {
        LOG.info("Order {} already processed, skipping", command.orderId());
        return orderRepository
                .findByOrderId(command.orderId())
                .switchIfEmpty(
                        Mono.error(
                                new IllegalStateException(
                                        "Order "
                                                + command.orderId()
                                                + " reported as existing but could not be loaded")));
    }

    private Mono<Order> enrichAndPersist(ProcessOrderCommand command) {
        Mono<Customer> customerMono = customerProvider.getCustomer(command.customerId());
        Mono<Map<String, Product>> productsMono = fetchProducts(command);

        return Mono.zip(customerMono, productsMono)
                .map(t -> assembleValidated(command, t.getT1(), t.getT2()))
                .flatMap(orderRepository::save)
                .doOnSuccess(order -> LOG.info("Order {} processed", order.orderId()))
                .doOnError(
                        err -> LOG.warn("Order {} failed: {}", command.orderId(), err.toString()));
    }

    private Mono<Map<String, Product>> fetchProducts(ProcessOrderCommand command) {
        List<String> productIds =
                command.lines().stream()
                        .map(ProcessOrderCommand.Line::productId)
                        .distinct()
                        .toList();
        return Flux.fromIterable(productIds)
                .flatMap(productProvider::getProduct, ENRICHMENT_CONCURRENCY)
                .collect(Collectors.toMap(Product::productId, Function.identity()));
    }

    private Order assembleValidated(
            ProcessOrderCommand command, Customer customer, Map<String, Product> productById) {
        validator.requireActiveCustomer(customer);
        validator.requireAllProductsResolved(
                command.lines().stream().map(ProcessOrderCommand.Line::productId).toList(),
                productById);
        return assembler.assemble(command, customer, productById);
    }
}
