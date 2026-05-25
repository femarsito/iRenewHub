package com.irenewhub.backend.service;

import com.irenewhub.backend.dto.CreateOrderRequest;
import com.irenewhub.backend.dto.OrderResponse;
import com.irenewhub.backend.entity.Coupon;
import com.irenewhub.backend.entity.Order;
import com.irenewhub.backend.entity.OrderItem;
import com.irenewhub.backend.entity.Product;
import com.irenewhub.backend.entity.User;
import com.irenewhub.backend.exception.ResourceNotFoundException;
import com.irenewhub.backend.repository.CouponRepository;
import com.irenewhub.backend.repository.OrderRepository;
import com.irenewhub.backend.repository.ProductRepository;
import com.irenewhub.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;

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

        // 4. Aplicar cupón de descuento si se ha proporcionado
        Coupon usedCoupon = null;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            Coupon coupon = couponRepository
                    .findByCodeAndUserEmailAndUsedFalse(request.getCouponCode().trim().toUpperCase(), userEmail)
                    .orElseThrow(() -> new RuntimeException("Cupón no válido o ya utilizado"));

            BigDecimal discount = BigDecimal.valueOf(coupon.getDiscountPercent()).divide(new BigDecimal("100"));
            total = total.subtract(total.multiply(discount)).setScale(2, RoundingMode.HALF_UP);
            usedCoupon = coupon;
        }

        // 5. Crear el pedido
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

        // 6. Asociar items al pedido
        items.forEach(item -> item.setOrder(order));
        order.setItems(items);

        Order saved = orderRepository.save(order);

        // 7. Marcar el cupón como usado (después de guardar el pedido con éxito)
        if (usedCoupon != null) {
            usedCoupon.setUsed(true);
            couponRepository.save(usedCoupon);
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String userEmail) {
        return orderRepository.findByUserEmailOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ---- PANEL ADMIN ----

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Cambia el estado de un pedido y ajusta el stock cuando hace falta.
    //   - Cancelar pedido    -> suma las unidades al stock
    //   - Reactivar cancelado -> resta las unidades (valida que haya suficiente)
    // El resto de transiciones (PENDING->CONFIRMED->SHIPPED->DELIVERED) no tocan stock.
    // @Transactional → si algo falla, todo se revierte.
    @Transactional
    public OrderResponse updateOrderStatus(Long id, String statusString) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + id));

        Order.OrderStatus estadoAnterior = order.getStatus();
        Order.OrderStatus estadoNuevo = Order.OrderStatus.valueOf(statusString);

        // Si el estado no cambia, no hay nada que hacer
        if (estadoAnterior == estadoNuevo) {
            return toResponse(order);
        }

        // Cargamos los items con JOIN FETCH para evitar lazy loading y el problema N+1
        List<OrderItem> items = orderRepository.findItemsByOrderId(id);

        // CANCELAR → devolver stock (delta positivo)
        if (estadoNuevo == Order.OrderStatus.CANCELLED && estadoAnterior != Order.OrderStatus.CANCELLED) {
            for (OrderItem item : items) {
                productRepository.updateStock(item.getProduct().getId(), item.getQuantity());
            }
        }

        // REACTIVAR pedido cancelado -> descontar stock (delta negativo)
        // Validamos antes porque entre la cancelación y la reactivación otros
        // clientes pueden haber comprado las unidades devueltas.
        if (estadoAnterior == Order.OrderStatus.CANCELLED && estadoNuevo != Order.OrderStatus.CANCELLED) {
            for (OrderItem item : items) {
                Product product = productRepository.findById(item.getProduct().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

                if (product.getStock() < item.getQuantity()) {
                    throw new RuntimeException(
                        "Stock insuficiente para reactivar: " + product.getName() +
                        " (disponible: " + product.getStock() + ", necesario: " + item.getQuantity() + ")");
                }
                productRepository.updateStock(item.getProduct().getId(), -item.getQuantity());
            }
        }

        order.setStatus(estadoNuevo);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + id));
        // Solo se pueden borrar pedidos entregados o cancelados
        if (order.getStatus() != Order.OrderStatus.DELIVERED &&
            order.getStatus() != Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("Solo se pueden eliminar pedidos entregados o cancelados");
        }
        orderRepository.delete(order);
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
                .customerName(order.getShippingFirstName() + " " + order.getShippingLastName())
                .customerEmail(order.getShippingEmail())
                .build();
    }
}