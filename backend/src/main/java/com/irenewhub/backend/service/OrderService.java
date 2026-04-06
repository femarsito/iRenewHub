package com.irenewhub.backend.service;

import com.irenewhub.backend.dto.CreateOrderRequest;
import com.irenewhub.backend.dto.OrderResponse;
import com.irenewhub.backend.entity.Order;
import com.irenewhub.backend.entity.OrderItem;
import com.irenewhub.backend.entity.Product;
import com.irenewhub.backend.entity.User;
import com.irenewhub.backend.exception.ResourceNotFoundException;
import com.irenewhub.backend.repository.OrderRepository;
import com.irenewhub.backend.repository.ProductRepository;
import com.irenewhub.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String userEmail) {

        // 1. Obtener el usuario autenticado
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 2. Construir los items y calcular subtotal
        List<OrderItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado con id: " + itemRequest.getProductId()));

            // Verificar stock disponible
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new RuntimeException("Stock insuficiente para: " + product.getName());
            }

            // Descontar stock
            product.setStock(product.getStock() - itemRequest.getQuantity());
            productRepository.save(product);

            BigDecimal itemSubtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(itemSubtotal)
                    .build();

            items.add(orderItem);
            subtotal = subtotal.add(itemSubtotal);
        }

        // 3. Calcular envío (gratis si subtotal >= 50€)
        BigDecimal shippingCost = subtotal.compareTo(new BigDecimal("50.00")) >= 0
                ? BigDecimal.ZERO
                : new BigDecimal("4.99");

        BigDecimal total = subtotal.add(shippingCost);

        // 4. Crear el pedido
        Order order = Order.builder()
                .orderNumber("IPH-" + UUID.randomUUID().toString()
                        .substring(0, 8).toUpperCase())
                .user(user)
                .subtotal(subtotal)
                .shippingCost(shippingCost)
                .total(total)
                .status(Order.OrderStatus.PENDING)
                .shippingFirstName(request.getFirstName())
                .shippingLastName(request.getLastName())
                .shippingEmail(request.getEmail())
                .shippingPhone(request.getPhone())
                .shippingAddress(request.getAddress())
                .shippingCity(request.getCity())
                .shippingPostalCode(request.getPostalCode())
                .shippingProvince(request.getProvince())
                .build();

        // 5. Asociar items al pedido
        items.forEach(item -> item.setOrder(order));
        order.setItems(items);

        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String userEmail) {
        return orderRepository.findByUserEmailOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private OrderResponse toResponse(Order order) {
        List<OrderResponse.OrderItemResponse> itemResponses = order.getItems()
                .stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().name())
                .subtotal(order.getSubtotal())
                .shippingCost(order.getShippingCost())
                .total(order.getTotal())
                .createdAt(order.getCreatedAt())
                .items(itemResponses)
                .build();
    }
}