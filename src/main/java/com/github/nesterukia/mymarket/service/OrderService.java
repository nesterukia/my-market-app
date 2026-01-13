package com.github.nesterukia.mymarket.service;

import com.github.nesterukia.mymarket.dao.OrderItemRepository;
import com.github.nesterukia.mymarket.dao.OrderRepository;
import com.github.nesterukia.mymarket.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElseThrow();
    }

    public Order createOrderFromCart(Cart cart) {
        Order newOrder = orderRepository.save(new Order());
        cart.getCartItems().forEach(cartItem -> orderItemRepository.save(
                new OrderItem(newOrder, cartItem.getItem(), cartItem.getQuantity())
        ));
        return newOrder;
    }
}
