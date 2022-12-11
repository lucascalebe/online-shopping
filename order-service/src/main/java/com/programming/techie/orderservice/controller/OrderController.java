package com.programming.techie.orderservice.controller;

import com.programming.techie.orderservice.client.InventoryClient;
import com.programming.techie.orderservice.dto.OrderDto;
import com.programming.techie.orderservice.model.Order;
import com.programming.techie.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreaker;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@RestController
@RequestMapping("api/order")
@RequiredArgsConstructor
public class OrderController {

  private final OrderRepository orderRepository;
  private final InventoryClient inventoryClient;
  private final Resilience4JCircuitBreakerFactory circuitBreakerFactory;

  @PostMapping
  public String placeOrder(@RequestBody OrderDto orderDto) {
    Resilience4JCircuitBreaker circuitBreaker = circuitBreakerFactory.create("invenotry");
    Supplier<Boolean> booleanSupplier = () -> orderDto.getOrderLineItems().stream()
            .allMatch(orderLineItem -> inventoryClient.isInStock(orderLineItem.getSkuCode()));

    boolean allProductsInStock = circuitBreaker.run(booleanSupplier, throwable -> handleErrorCase());

    if (allProductsInStock) {
      Order order = new Order();
      order.setOrderLineItems(orderDto.getOrderLineItems());
      order.setOrderNumber(UUID.randomUUID().toString());

      orderRepository.save(order);

      return "Order place successfully";
    }

    return "Order failed, one of the products in the order is not in stock";
  }

  private Boolean handleErrorCase() {
    return false;
  }
}
