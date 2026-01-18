package com.github.nesterukia.mymarket.http;

import com.github.nesterukia.mymarket.domain.Cart;
import com.github.nesterukia.mymarket.domain.Order;
import com.github.nesterukia.mymarket.domain.User;
import com.github.nesterukia.mymarket.http.models.OrderDto;
import com.github.nesterukia.mymarket.service.CartService;
import com.github.nesterukia.mymarket.service.OrderService;
import com.github.nesterukia.mymarket.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

import static com.github.nesterukia.mymarket.utils.UserUtils.USER_ID_COOKIE;

@Controller
public class OrderController {
    private final OrderService orderService;
    private final CartService cartService;
    private final UserService userService;

    @Autowired
    public OrderController(OrderService orderService, CartService cartService, UserService userService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping("/orders")
    public String getAllOrders(Model model, @CookieValue(value = USER_ID_COOKIE, required = false) Long userId) {
        List<Order> allOrders = (userId == null) ? List.of() : orderService.getAllOrders(userId);
        model.addAttribute("orders", allOrders.stream().map(OrderDto::fromOrder).toList());
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String getOrder(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean newOrder,
            @CookieValue(value = USER_ID_COOKIE, required = false) Long userId,
            Model model) {
        Order order = orderService.getOrderById(id);

        if (!order.getUser().getId().equals(userId)) {
            throw new NoSuchElementException("No access for this order.");
        }

        model.addAttribute("order", OrderDto.fromOrder(order));
        model.addAttribute("newOrder", newOrder);
        return "order";
    }

    @PostMapping("/buy")
    public String buy(@CookieValue(value = USER_ID_COOKIE, required = false) Long userId, HttpServletResponse response) {
        User user = userService.getOrCreate(userId, response);
        Cart cart = user.getCart() != null ? user.getCart() : cartService.create(user);
        Order newOrder = orderService.createOrderFromCart(cart);
        cartService.clearCartAndDelete(cart);
        String redirectTemplate = "redirect:/orders/%d?newOrder=true";
        return redirectTemplate.formatted(newOrder.getId());
    }
}
