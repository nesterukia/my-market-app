package com.github.nesterukia.mymarket.http;

import com.github.nesterukia.mymarket.domain.ActionType;
import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.domain.SortType;
import com.github.nesterukia.mymarket.domain.User;
import com.github.nesterukia.mymarket.http.dto.ItemDto;
import com.github.nesterukia.mymarket.http.dto.ItemsRequestDto;
import com.github.nesterukia.mymarket.http.dto.payment.PaymentInfo;
import com.github.nesterukia.mymarket.service.CartService;
import com.github.nesterukia.mymarket.service.ItemService;
import com.github.nesterukia.mymarket.service.PaymentService;
import com.github.nesterukia.mymarket.service.UserService;
import com.github.nesterukia.mymarket.utils.ItemUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.List;

import static com.github.nesterukia.mymarket.utils.UserUtils.USER_ID_COOKIE;

@Slf4j
@Controller
public class CartController {
    private final CartService cartService;
    private final ItemService itemService;
    private final UserService userService;
    private final PaymentService paymentService;

    @Autowired
    public CartController(CartService cartService, ItemService itemService, UserService userService, PaymentService paymentService) {
        this.cartService = cartService;
        this.itemService = itemService;
        this.userService = userService;
        this.paymentService = paymentService;
    }

    @GetMapping(value = "/cart/items")
    public Mono<Rendering> getCartItems(@CookieValue(value = USER_ID_COOKIE, required = false) Long userId,
                                        ServerWebExchange exchange,
                                        @RequestParam(defaultValue = "false") boolean error,
                                        @RequestParam(defaultValue = "") String errorMessage
    ) {
        return userService.getOrCreate(userId, exchange)
                .flatMap(user -> cartService.findByUserId(user.getId())
                        .switchIfEmpty(cartService.create(user)))
                .flatMapMany(cartService::findAllCartItemsByCart)
                .flatMap(cartItem -> {
                        Long itemId = cartItem.getItemId();
                        Integer itemQuantity = cartItem.getQuantity();
                        return itemService.getItemById(itemId).map(item -> Tuples.of(item, itemQuantity));
                })
                .map(itemQuantityTuple -> ItemDto.fromItem(
                        itemQuantityTuple.getT1(),
                        itemQuantityTuple.getT2()
                ))
                .collectList()
                .flatMap(itemsDtoList -> {
                    Double totalSum = ItemUtils.calculateTotalSum(itemsDtoList);
                    return paymentService.checkUserBalance(userId, totalSum)
                            .map(paymentInfo -> {
                                log.debug("CartController.getCartItems(): {}, {}", paymentInfo.isEnoughMoney(), paymentInfo.isServiceAvailable());
                                return this.renderCartViewFromItemDtos(
                                        itemsDtoList,
                                        totalSum,
                                        paymentInfo,
                                        error,
                                        errorMessage
                                );
                            });
                });
    }

    @PostMapping(value = "/cart/items")
    public Mono<Rendering> changeItemQuantityInCartFromCartPage(
            @ModelAttribute ItemsRequestDto itemsRequestDto,
            @CookieValue(value = USER_ID_COOKIE, required = false) Long userId,
            ServerWebExchange exchange) {

        ActionType actionType = ActionType.valueOf(itemsRequestDto.action().toUpperCase());
        return Mono.zip(itemService.getItemById(itemsRequestDto.id()), userService.getOrCreate(userId, exchange))
                .flatMapMany(tuple -> {
                    Item item = tuple.getT1();
                    User user = tuple.getT2();

                    return cartService.findByUserId(user.getId())
                            .switchIfEmpty(cartService.create(user))
                            .flatMapMany(cart -> cartService.updateItemQuantityInCart(actionType, cart, item, true)
                                    .thenMany(cartService.findAllCartItemsByCart(cart))
                            );
                })
                .flatMap(cartItem -> {
                    Long itemId = cartItem.getItemId();
                    Integer itemQuantity = cartItem.getQuantity();
                    return itemService.getItemById(itemId).zipWith(Mono.just(itemQuantity));
                })
                .map(itemQuantityTuple -> ItemDto.fromItem(
                        itemQuantityTuple.getT1(), itemQuantityTuple.getT2()
                ))
                .collectList()
                .flatMap(itemsDtoList -> {
                    Double totalSum = ItemUtils.calculateTotalSum(itemsDtoList);
                    return paymentService.checkUserBalance(userId, totalSum)
                            .map(paymentInfo -> this.renderCartViewFromItemDtos(
                                    itemsDtoList,
                                    totalSum,
                                    paymentInfo,
                                    false,
                                    ""
                            ));
                });
    }

    @PostMapping(value = "/items")
    public Mono<String> changeItemQuantityInCartFromItemsInCartPage(
            @ModelAttribute ItemsRequestDto itemsRequestDto,
            @CookieValue(value = USER_ID_COOKIE, required = false) Long userId,
            ServerWebExchange exchange) {

        SortType sortType = SortType.valueOf(itemsRequestDto.sort().toUpperCase());
        ActionType actionType = ActionType.valueOf(itemsRequestDto.action().toUpperCase());
        String redirectLinkTemplate = "redirect:/items?search=%s&sort=%s&pageNumber=%d&pageSize=%d";
        String redirectLink = redirectLinkTemplate.formatted(itemsRequestDto.search(), sortType.toString(), itemsRequestDto.pageNumber(),
                itemsRequestDto.pageSize());

        return Mono.zip(itemService.getItemById(itemsRequestDto.id()), userService.getOrCreate(userId, exchange))
                .flatMapMany(tuple -> {
                    Item item = tuple.getT1();
                    User user = tuple.getT2();

                    return cartService.findByUserId(user.getId())
                            .switchIfEmpty(cartService.create(user))
                            .flatMapMany(cart -> cartService.updateItemQuantityInCart(actionType, cart, item, false)
                                    .thenMany(cartService.findAllCartItemsByCart(cart))
                            );
                })
                .then(Mono.just(redirectLink));
    }

    @PostMapping(value = "/items/{id}")
    public Mono<Rendering> changeItemQuantityInCartFromItemInCartPage(
            @PathVariable Long id,
            @ModelAttribute ItemsRequestDto itemsRequestDto,
            @CookieValue(value = USER_ID_COOKIE, required = false) Long userId,
            ServerWebExchange exchange) {

        ActionType actionType = ActionType.valueOf(itemsRequestDto.action().toUpperCase());

        return Mono.zip(itemService.getItemById(id), userService.getOrCreate(userId, exchange))
                .flatMap(itemUserTuple -> {
                    Item item = itemUserTuple.getT1();
                    User user = itemUserTuple.getT2();

                    return cartService.findByUserId(user.getId())
                            .switchIfEmpty(cartService.create(user))
                            .flatMap(cart -> {
                                cartService.updateItemQuantityInCart(actionType, cart, item, false);
                                itemService.getItemById(id);
                                return Mono.zip(
                                        itemService.getItemById(id),
                                        cartService.countCartItemsByCartIdAndItemId(cart.getId(), id)
                                );
                            })
                            .map(itemQuantityTuple -> Rendering.view("item")
                                    .modelAttribute("item", ItemDto.fromItem(
                                            itemQuantityTuple.getT1(),
                                            itemQuantityTuple.getT2())
                                    )
                                    .build()
                            );
                });
    }

    private Rendering renderCartViewFromItemDtos(List<ItemDto> itemDtos, Double total, PaymentInfo paymentInfo, boolean error, String errorMessage) {
        Rendering.Builder<?> viewBuilder = Rendering.view("cart")
                .modelAttribute("items", itemDtos)
                .modelAttribute("total", total)
                .modelAttribute("error", error)
                .modelAttribute("errorMessage", errorMessage)
                .modelAttribute("paymentInfo", paymentInfo);
        for (int i = 0; i < itemDtos.size(); i++) {
            viewBuilder.modelAttribute("item%d".formatted(i + 1), itemDtos.get(i));
        }
        return viewBuilder.build();
    }
}
