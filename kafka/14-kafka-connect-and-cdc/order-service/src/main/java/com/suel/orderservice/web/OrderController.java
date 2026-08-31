package com.suel.orderservice.web;

import com.suel.orderservice.entity.Order;
import com.suel.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Deliberately thin: no service layer, no Kafka client, no outbox.
 * This controller saves a row and stops. Everything downstream happens
 * because Debezium is watching the write-ahead log.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        Order order = Order.builder()
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customerId(request.customerId())
                .product(request.product())
                .quantity(request.quantity())
                .amount(request.amount())
                .status("PENDING")
                .build();

        return ResponseEntity.ok(orderRepository.save(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Order> listOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<Map<String, String>> getStatus(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(o -> ResponseEntity.ok(Map.of(
                        "orderNumber", o.getOrderNumber(),
                        "status", o.getStatus())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** An UPDATE also lands in the WAL, so this produces a CDC event too. */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return orderRepository.findById(id)
                .map(o -> {
                    o.setStatus(status);
                    return ResponseEntity.ok(orderRepository.save(o));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record CreateOrderRequest(Long customerId, String product, Integer quantity, BigDecimal amount) {
    }
}
