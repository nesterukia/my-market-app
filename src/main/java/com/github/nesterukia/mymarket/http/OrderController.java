package com.github.nesterukia.mymarket.http;

import com.github.nesterukia.mymarket.domain.Cart;
import com.github.nesterukia.mymarket.domain.Order;
import com.github.nesterukia.mymarket.http.models.OrderDto;
import com.github.nesterukia.mymarket.service.CartService;
import com.github.nesterukia.mymarket.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class OrderController {
    private final OrderService orderService;
    private final CartService cartService;

    @Autowired
    public OrderController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @GetMapping("/orders")
    public String getAllOrders(Model model) {
        List<Order> allOrders = orderService.getAllOrders();
        model.addAttribute("orders", allOrders.stream().map(OrderDto::fromOrder).toList());
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String getOrder(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean newOrder,
            Model model) {
        Order order = orderService.getOrderById(id);
        model.addAttribute("order", OrderDto.fromOrder(order));
        model.addAttribute("newOrder", newOrder);
        return "order";
    }

    @PostMapping("/buy")
    public String buy() {
        Cart cart = cartService.getOrCreate();
        Order newOrder = orderService.createOrderFromCart(cart);
        cartService.clearCartAndDelete(cart);
        String redirectTemplate = "redirect:/orders/%d?newOrder=true";
        return redirectTemplate.formatted(newOrder.getId());
    }
}
