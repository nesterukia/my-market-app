package com.github.nesterukia.mymarket.http;

import com.github.nesterukia.mymarket.domain.exceptions.PaymentServerException;
import com.github.nesterukia.mymarket.http.dto.OrderDto;
import com.github.nesterukia.mymarket.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.github.nesterukia.mymarket.utils.UserUtils.USER_ID_COOKIE;

@Controller
public class OrderController {
    private final OrderService orderService;
    private final CartService cartService;
    private final UserService userService;
    private final ItemService itemService;
    private final PaymentService paymentService;

    @Autowired
    public OrderController(OrderService orderService, CartService cartService, UserService userService, ItemService itemService, PaymentService paymentService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.userService = userService;
        this.itemService = itemService;
        this.paymentService = paymentService;
    }

    @GetMapping("/orders")
    public Mono<Rendering> getAllOrders(@CookieValue(value = USER_ID_COOKIE, required = false) Long userId) {
        return orderService.getAllOrders(userId)
                .flatMap(order -> Mono.just(order).zipWith(itemService.findAllByOrderId(order.getId()).collectList()))
                .map(orderItemsTuple -> OrderDto.fromOrder(
                        orderItemsTuple.getT1(),
                        orderItemsTuple.getT2()
                ))
                .collectList()
                .map(ordersDtoList -> Rendering.view("orders")
                        .modelAttribute("orders", ordersDtoList)
                        .build()
                );
    }

    @GetMapping("/orders/{id}")
    public Mono<Rendering> getOrder(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean newOrder,
            @CookieValue(value = USER_ID_COOKIE, required = false) Long userId) {
        return orderService.getOrderByUserIdAndId(userId, id)
                .flatMap(order -> Mono.just(order).zipWith(itemService.findAllByOrderId(order.getId()).collectList()))
                .map(orderItemsTuple -> OrderDto.fromOrder(
                        orderItemsTuple.getT1(),
                        orderItemsTuple.getT2()
                ))
                .map(orderDto -> Rendering.view("order")
                        .modelAttribute("order", orderDto)
                        .modelAttribute("newOrder", newOrder)
                        .build()
                );
    }

    @PostMapping("/buy")
    public Mono<String> buy(@CookieValue(value = USER_ID_COOKIE, required = false) Long userId, ServerWebExchange exchange) {
        String successRedirectTemplate = "redirect:/orders/%d?newOrder=true";
        String errorRedirectTemplate = "redirect:/cart/items?error=true&errorMessage=%s";

        return userService.getOrCreate(userId, exchange)
                .flatMap(user -> cartService.findByUserId(user.getId()))
                .flatMap(cart ->
                        cartService.calculateTotalSum(cart)
                                .flatMap(cartSum -> paymentService.commitPayment(userId, cartSum))
                                .then(orderService.createOrderFromCart(cart))
                                .flatMap(newOrder -> cartService.clearCartAndDelete(cart)
                                                    .thenReturn(newOrder.getId())
                                )
                )
                .map(successRedirectTemplate::formatted)
                .onErrorResume(
                        PaymentServerException.class,
                        ex -> Mono.just(
                                errorRedirectTemplate.formatted(ex.getTransactionInfo().message().replace(" ", "%20"))
                        )
                );
    }
}
