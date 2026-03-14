package com.github.nesterukia.mymarket.http;

import com.github.nesterukia.mymarket.domain.ActionType;
import com.github.nesterukia.mymarket.domain.SortType;
import com.github.nesterukia.mymarket.http.dto.ItemDto;
import com.github.nesterukia.mymarket.http.dto.ItemsRequestDto;
import com.github.nesterukia.mymarket.http.dto.payment.PaymentInfo;
import com.github.nesterukia.mymarket.service.CartService;
import com.github.nesterukia.mymarket.service.ItemService;
import com.github.nesterukia.mymarket.service.PaymentService;
import com.github.nesterukia.mymarket.utils.ItemUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.List;

@Slf4j
@Controller
public class CartController {
    private final CartService cartService;
    private final ItemService itemService;
    private final PaymentService paymentService;

    @Autowired
    public CartController(CartService cartService, ItemService itemService, PaymentService paymentService) {
        this.cartService = cartService;
        this.itemService = itemService;
        this.paymentService = paymentService;
    }

    @GetMapping(value = "/cart/items")
    public Mono<Rendering> getCartItems(@AuthenticationPrincipal OAuth2User oAuth2User,
                                        @RequestParam(defaultValue = "false") boolean error,
                                        @RequestParam(defaultValue = "") String errorMessage
    ) {

        String userId = oAuth2User.getAttribute("sub");

        return cartService.findByUserId(userId)
                .switchIfEmpty(cartService.create(userId))
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
            @AuthenticationPrincipal OAuth2User oAuth2User) {

        String userId = oAuth2User.getAttribute("sub");
        ActionType actionType = ActionType.valueOf(itemsRequestDto.action().toUpperCase());
        return itemService.getItemById(itemsRequestDto.id())
                .flatMapMany(item -> cartService.findByUserId(userId)
                        .switchIfEmpty(cartService.create(userId))
                        .flatMapMany(cart -> cartService.updateItemQuantityInCart(actionType, cart, item, true)
                                .thenMany(cartService.findAllCartItemsByCart(cart))
                        ))
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
            @AuthenticationPrincipal OAuth2User oAuth2User) {

        String userId = oAuth2User.getAttribute("sub");
        SortType sortType = SortType.valueOf(itemsRequestDto.sort().toUpperCase());
        ActionType actionType = ActionType.valueOf(itemsRequestDto.action().toUpperCase());
        String redirectLinkTemplate = "redirect:/items?search=%s&sort=%s&pageNumber=%d&pageSize=%d";
        String redirectLink = redirectLinkTemplate.formatted(itemsRequestDto.search(), sortType.toString(), itemsRequestDto.pageNumber(),
                itemsRequestDto.pageSize());

        return itemService.getItemById(itemsRequestDto.id())
                .flatMapMany(item -> cartService.findByUserId(userId)
                        .switchIfEmpty(cartService.create(userId))
                        .flatMapMany(cart -> cartService.updateItemQuantityInCart(actionType, cart, item, false)
                                .thenMany(cartService.findAllCartItemsByCart(cart))
                        ))
                .then(Mono.just(redirectLink));
    }

    @PostMapping(value = "/items/{id}")
    public Mono<Rendering> changeItemQuantityInCartFromItemInCartPage(
            @PathVariable Long id,
            @ModelAttribute ItemsRequestDto itemsRequestDto,
            @AuthenticationPrincipal OAuth2User oAuth2User) {

        String userId = oAuth2User.getAttribute("sub");
        ActionType actionType = ActionType.valueOf(itemsRequestDto.action().toUpperCase());

        return itemService.getItemById(id)
                .flatMap(item -> cartService.findByUserId(userId)
                        .switchIfEmpty(cartService.create(userId))
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
                        )
                );
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
