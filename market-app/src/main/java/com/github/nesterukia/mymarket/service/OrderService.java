package com.github.nesterukia.mymarket.service;

import com.github.nesterukia.mymarket.dao.CartItemRepository;
import com.github.nesterukia.mymarket.dao.OrderItemRepository;
import com.github.nesterukia.mymarket.dao.OrderRepository;
import com.github.nesterukia.mymarket.domain.Cart;
import com.github.nesterukia.mymarket.domain.Order;
import com.github.nesterukia.mymarket.domain.OrderItem;
import com.github.nesterukia.mymarket.domain.exceptions.EntityNotFoundException;
import com.github.nesterukia.mymarket.utils.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, CartItemRepository cartItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public Flux<Order> getAllOrders(Long userId) {
        if (userId == null) {
            return Flux.empty();
        }
        return orderRepository.findAllByUserId(userId);
    }

    public Mono<Order> getOrderByUserIdAndId(Long userId, Long id) {
        return orderRepository.findByIdAndUserId(id, userId).switchIfEmpty(
                Mono.error(new EntityNotFoundException(EntityType.Order, id))
        );
    }

    public Mono<Order> createOrderFromCart(Cart cart) {
        Order orderFromCart = Order.builder()
                .userId(cart.getUserId())
                .build();
        return orderRepository.save(orderFromCart)
                .flatMap(savedOrder -> saveOrderItems(savedOrder, cart));
    }

    private Mono<Order> saveOrderItems(Order order, Cart cart) {
        return cartItemRepository.findAllByCartId(cart.getId())
                .map(cartItem -> new OrderItem(order.getId(), cartItem.getItemId(), cartItem.getQuantity()))
                .flatMap(orderItemRepository::save)
                .then(Mono.just(order));
    }
}
