package com.github.nesterukia.mymarket.http;

import com.github.nesterukia.mymarket.domain.exceptions.PaymentServerException;
import com.github.nesterukia.mymarket.http.dto.OrderDto;
import com.github.nesterukia.mymarket.service.CartService;
import com.github.nesterukia.mymarket.service.ItemService;
import com.github.nesterukia.mymarket.service.OrderService;
import com.github.nesterukia.mymarket.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class OrderController {
    private final OrderService orderService;
    private final CartService cartService;
    private final ItemService itemService;
    private final PaymentService paymentService;

    @Autowired
    public OrderController(OrderService orderService, CartService cartService, ItemService itemService, PaymentService paymentService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.itemService = itemService;
        this.paymentService = paymentService;
    }

    @GetMapping("/orders")
    public Mono<Rendering> getAllOrders(@AuthenticationPrincipal OAuth2User oAuth2User) {
        String userId = oAuth2User.getAttribute("sub");

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
                                    @AuthenticationPrincipal OAuth2User oAuth2User) {

        String userId = oAuth2User.getAttribute("sub");

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
    public Mono<String> buy(@AuthenticationPrincipal OAuth2User oAuth2User) {
        String successRedirectTemplate = "redirect:/orders/%d?newOrder=true";
        String errorRedirectTemplate = "redirect:/cart/items?error=true&errorMessage=%s";

        String userId = oAuth2User.getAttribute("sub");
        return cartService.findByUserId(userId)
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
