package com.github.nesterukia.mymarket.http;

import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.domain.SortType;
import com.github.nesterukia.mymarket.http.dto.ItemDto;
import com.github.nesterukia.mymarket.http.dto.ItemsDto;
import com.github.nesterukia.mymarket.http.dto.Paging;
import com.github.nesterukia.mymarket.service.CartService;
import com.github.nesterukia.mymarket.service.ItemService;
import com.github.nesterukia.mymarket.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Controller
@Slf4j
public class ItemController {

    private final ItemService itemService;
    private final CartService cartService;

    @Autowired
    public ItemController(ItemService itemService, CartService cartService) {
        this.itemService = itemService;
        this.cartService = cartService;
    }

    @GetMapping(value = {"/items", "/"})
    public Mono<Rendering> getItems(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") String sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize,
            @AuthenticationPrincipal OAuth2User oAuth2User) {

        String userId = oAuth2User != null ? oAuth2User.getAttribute("sub") : UserUtils.ANONYMOUS_USER_ID;

        SortType sortType = SortType.valueOf(sort.toUpperCase());
        Sort sortBy = switch (sortType) {
            case ALPHA  -> Sort.by(Sort.Direction.ASC, "title");
            case PRICE -> Sort.by(Sort.Direction.ASC, "price");
            default -> Sort.unsorted();
        };
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sortBy);

        return itemService.getItems(search, pageable)
                .flatMap(items -> itemService.formItemsPage(items, search, pageable))
                .flatMap(page -> Flux.fromIterable(page.getContent())
                        .flatMap(item -> Mono.just(item).zipWith(
                                cartService.findByUserId(userId)
                                        .flatMap(cart -> cartService.countCartItemsByUserIdAndItemId(
                                                userId,
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
                        ))).map(itemsDto -> Rendering.view("items")
                        .modelAttribute("items", itemsDto.getItems())
                        .modelAttribute("search", itemsDto.getSearch())
                        .modelAttribute("sort", itemsDto.getSort().toString())
                        .modelAttribute("paging", itemsDto.getPaging())
                        .modelAttribute("isAuthenticated", !Objects.equals(userId, UserUtils.ANONYMOUS_USER_ID))
                        .build()
                );
    }

    @GetMapping(value = "/items/{id}")
    public Mono<Rendering> getItemById(@PathVariable Long id,
                                       @AuthenticationPrincipal OAuth2User oAuth2User) {

        String userId = oAuth2User != null ? oAuth2User.getAttribute("sub") : UserUtils.ANONYMOUS_USER_ID;

        return Mono.zip(
                itemService.getItemById(id),
                cartService.countCartItemsByUserIdAndItemId(userId, id)
        ).map(tuple -> {
            Item item = tuple.getT1();
            Integer quantity = tuple.getT2();
            ItemDto itemDto = ItemDto.fromItem(item, quantity);
            return Rendering.view("item")
                    .modelAttribute("item", itemDto)
                    .modelAttribute("isAuthenticated", !Objects.equals(userId, UserUtils.ANONYMOUS_USER_ID))
                    .build();
        });
    }
}
