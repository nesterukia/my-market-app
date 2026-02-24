package com.github.nesterukia.mymarket.http;

import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.domain.SortType;
import com.github.nesterukia.mymarket.domain.User;
import com.github.nesterukia.mymarket.http.dto.ItemDto;
import com.github.nesterukia.mymarket.http.dto.ItemsDto;
import com.github.nesterukia.mymarket.http.dto.Paging;
import com.github.nesterukia.mymarket.service.CartService;
import com.github.nesterukia.mymarket.service.ItemService;
import com.github.nesterukia.mymarket.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import static com.github.nesterukia.mymarket.utils.UserUtils.USER_ID_COOKIE;

@Controller
@Slf4j
public class ItemController {

    private final UserService userService;
    private final ItemService itemService;
    private final CartService cartService;

    @Autowired
    public ItemController(ItemService itemService, CartService cartService,
                          UserService userService) {
        this.itemService = itemService;
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping(value = {"/items", "/"})
    public Mono<Rendering> getItems(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") String sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize,
            @CookieValue(value = USER_ID_COOKIE, required = false) Long userId,
            ServerWebExchange exchange) {

        SortType sortType = SortType.valueOf(sort.toUpperCase());
        Sort sortBy = switch (sortType) {
            case ALPHA  -> Sort.by(Sort.Direction.ASC, "title");
            case PRICE -> Sort.by(Sort.Direction.ASC, "price");
            default -> Sort.unsorted();
        };
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sortBy);

        return itemService.getItems(search, pageable)
                .flatMap(items -> itemService.formItemsPage(items, search, pageable))
                    .zipWith(userService.getOrCreate(userId, exchange))
                    .flatMap(pageUserTuple -> {
                        Page<Item> page = pageUserTuple.getT1();
                        User user = pageUserTuple.getT2();
                        return Flux.fromIterable(page.getContent())
                                .flatMap(item -> Mono.just(item).zipWith(
                                        cartService.findByUserId(user.getId())
                                                .flatMap(cart -> cartService.countCartItemsByUserIdAndItemId(
                                                        user.getId(),
                                                        item.getId())
                                                )
                                                .switchIfEmpty(Mono.just(0))
                                ))
                                .map(tuple -> {
                                    Item item = tuple.getT1();
                                    Integer quantity = tuple.getT2();
                                    return ItemDto.fromItem(item, quantity);
                                })
                                .collectList()
                                .map(listOfItemDtos -> ItemsDto.fromListOfItemDtos(
                                        listOfItemDtos,
                                        search,
                                        sortType,
                                        new Paging(pageSize, pageNumber, page.hasPrevious(), page.hasNext())
                                ));
                    }).map(itemsDto -> Rendering.view("items")
                            .modelAttribute("items", itemsDto.getItems())
                            .modelAttribute("search", itemsDto.getSearch())
                            .modelAttribute("sort", itemsDto.getSort().toString())
                            .modelAttribute("paging", itemsDto.getPaging())
                            .build());
    }

    @GetMapping(value = "/items/{id}")
    public Mono<Rendering> getItemById(@PathVariable Long id,
                                       @CookieValue(value = USER_ID_COOKIE, required = false) Long userId,
                                       ServerWebExchange exchange) {
        return userService.getOrCreate(userId, exchange).flatMap( user -> Mono.zip(
                itemService.getItemById(id),
                cartService.countCartItemsByUserIdAndItemId(user.getId(), id)
        )).map(tuple -> {
            Item item = tuple.getT1();
            Integer quantity = tuple.getT2();
            ItemDto itemDto = ItemDto.fromItem(item, quantity);
            return Rendering.view("item")
                    .modelAttribute("item", itemDto)
                    .build();
        });
    }
}
