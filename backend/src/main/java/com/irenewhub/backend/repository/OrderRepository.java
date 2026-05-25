package com.irenewhub.backend.repository;

// ---- REPOSITORIO DE PEDIDOS ----
// Interfaz que gestiona las consultas a la tabla orders y order_items.
// Los metodos con nombre derivado los genera Spring automaticamente.
// Los metodos con @Query usan JPQL: trabaja con clases Java (Order, OrderItem)
// en vez de nombres de tablas SQL (orders, order_items).

import com.irenewhub.backend.entity.Order;
import com.irenewhub.backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // registra esta interfaz como componente de Spring para inyectarla con @Autowired
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Spring genera: SELECT * FROM orders WHERE user_email = ? ORDER BY created_at DESC
    // Se usa en /api/orders/my-orders para que el usuario vea solo sus propios pedidos
    List<Order> findByUserEmailOrderByCreatedAtDesc(String email);

    // Spring genera: SELECT * FROM orders ORDER BY created_at DESC
    // Se usa en el panel admin para ver todos los pedidos de todos los usuarios
    List<Order> findAllByOrderByCreatedAtDesc();

    // Cuenta pedidos no cancelados de un usuario que contienen un producto concreto
    // Se usa para saber si el usuario puede dejar reseña (solo si ha comprado el producto)
    // JOIN o.items i → une la tabla orders con order_items
    // o.status <> CANCELLED → excluye pedidos cancelados
    @Query("SELECT COUNT(o) FROM Order o JOIN o.items i WHERE o.user.email = :email AND i.product.id = :productId AND o.status <> com.irenewhub.backend.entity.Order.OrderStatus.CANCELLED")
    long countPurchaseByUserAndProduct(@Param("email") String email, @Param("productId") Long productId);

    // Cuenta cuantos pedidos contienen un producto concreto
    // Se usa antes de eliminar un producto: si tiene pedidos asociados no se puede borrar
    @Query("SELECT COUNT(i) FROM OrderItem i WHERE i.product.id = :productId")
    long countOrdersByProductId(@Param("productId") Long productId);

    // Devuelve los items de un pedido cargando tambien el producto de cada item (JOIN FETCH)
    // JOIN FETCH → carga el producto en la misma query, evita el problema de lazy loading
    // donde order.getItems() puede devolver lista vacia si Hibernate no ha cargado la relacion
    // Se usa para restaurar o descontar stock al cancelar o reactivar un pedido
    @Query("SELECT i FROM OrderItem i JOIN FETCH i.product WHERE i.order.id = :orderId")
    List<OrderItem> findItemsByOrderId(@Param("orderId") Long orderId);
}