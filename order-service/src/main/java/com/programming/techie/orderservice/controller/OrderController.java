package com.programming.techie.orderservice.controller;

import com.programming.techie.orderservice.client.InventoryClient;
import com.programming.techie.orderservice.dto.OrderDto;
import com.programming.techie.orderservice.model.Order;
import com.programming.techie.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("api/order")
@RequiredArgsConstructor
public class OrderController {

  private final OrderRepository orderRepository;
  private final InventoryClient inventoryClient;

  @PostMapping
  public String placeOrder(@RequestBody OrderDto orderDto) {
    boolean allProductsInStock = orderDto.getOrderLineItems().stream()
            .allMatch(orderLineItem -> inventoryClient.isInStock(orderLineItem.getSkuCode()));

    if (allProductsInStock) {
      Order order = new Order();
      order.setOrderLineItems(orderDto.getOrderLineItems());
      order.setOrderNumber(UUID.randomUUID().toString());

      orderRepository.save(order);

      return "Order place successfully";
    }

    return "Order failed, one of the products in the order is not in stock";
  }
}
