package com.github.nesterukia.mymarket.http;

import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.domain.SortType;
import com.github.nesterukia.mymarket.http.models.ItemDto;
import com.github.nesterukia.mymarket.http.models.ItemsDto;
import com.github.nesterukia.mymarket.http.models.Paging;
import com.github.nesterukia.mymarket.service.CartService;
import com.github.nesterukia.mymarket.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.github.nesterukia.mymarket.utils.UserUtils.USER_ID_COOKIE;

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
            @CookieValue(value = USER_ID_COOKIE, required = false) Long userId) {

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
                            cartService.countCartItemsByUserIdAndItemId(userId, item.getId())
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
                        ))
                ).map(itemsDto -> Rendering.view("items")
                        .modelAttribute("items", itemsDto.getItems())
                        .modelAttribute("search", itemsDto.getSearch())
                        .modelAttribute("sort", itemsDto.getSort().toString())
                        .modelAttribute("paging", itemsDto.getPaging())
                        .build());
    }

    @GetMapping(value = "/items/{id}")
    public Mono<Rendering> getItemById(@PathVariable Long id,
                                       @CookieValue(value = USER_ID_COOKIE, required = false) Long userId) {
        return Mono.zip(
                itemService.getItemById(id),
                cartService.countCartItemsByUserIdAndItemId(userId, id)
        ).map(tuple -> {
            Item item = tuple.getT1();
            Integer quantity = tuple.getT2();
            ItemDto itemDto = ItemDto.fromItem(item, quantity);
            return Rendering.view("item")
                    .modelAttribute("item", itemDto)
                    .build();
        });
    }
}
