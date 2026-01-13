package com.github.nesterukia.mymarket.http;

import com.github.nesterukia.mymarket.domain.*;
import com.github.nesterukia.mymarket.http.models.ItemDto;
import com.github.nesterukia.mymarket.service.CartService;
import com.github.nesterukia.mymarket.service.ItemService;
import com.github.nesterukia.mymarket.utils.ItemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CartController {
    private final CartService cartService;
    private final ItemService itemService;

    @Autowired
    public CartController(CartService cartService, ItemService itemService) {
        this.cartService = cartService;
        this.itemService = itemService;
    }

    @GetMapping(value = "/cart/items")
    public String getCartItems(Model model) {
        List<CartItem> cartItems = cartService.getOrCreate().getCartItems();
        List<ItemDto> listOfItemDtos = cartItems.stream()
                .map(ItemDto::fromCartItem)
                .toList();

        model.addAttribute("items", listOfItemDtos);
        for (int i = 0; i < listOfItemDtos.size(); i++) {
            String attributeName = "item%d".formatted(i + 1);
            model.addAttribute(attributeName, listOfItemDtos.get(i));
        }
        model.addAttribute("total", ItemUtils.calculateTotalSum(listOfItemDtos));
        return "cart";
    }

    @PostMapping(value = "/cart/items")
    public String changeItemQuantityInCartFromCartPage(
            @RequestParam Long id,
            @RequestParam String action,
            Model model) {

        ActionType actionType = ActionType.valueOf(action.toUpperCase());

        Item item = itemService.getItemById(id);
        Cart cart = cartService.getOrCreate();

        switch (actionType) {
            case MINUS -> cartService.decreaseItemQuantityInCart(cart, item);
            case PLUS -> cartService.increaseItemQuantityInCart(cart, item);
            case DELETE -> cartService.removeItemFromCart(cart, item);
        }

        List<CartItem> cartItems = cartService.getOrCreate().getCartItems();
        List<ItemDto> listOfItemDtos = cartItems.stream()
                .map(ItemDto::fromCartItem)
                .toList();

        model.addAttribute("items", listOfItemDtos);
        for (int i = 0; i < listOfItemDtos.size(); i++) {
            String attributeName = "item%d".formatted(i + 1);
            model.addAttribute(attributeName, listOfItemDtos.get(i));
        }
        model.addAttribute("total", ItemUtils.calculateTotalSum(listOfItemDtos));
        return "cart";
    }

    @PostMapping(value = "/items")
    public String changeItemQuantityInCartFromItemsInCartPage(
            @RequestParam(defaultValue = "") Long id,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") String sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam String action) {

        SortType sortType = SortType.valueOf(sort.toUpperCase());
        ActionType actionType = ActionType.valueOf(action.toUpperCase());
        Item item = itemService.getItemById(id);
        Cart cart = cartService.getOrCreate();

        switch (actionType) {
            case MINUS -> cartService.decreaseItemQuantityInCart(cart, item);
            case PLUS -> cartService.increaseItemQuantityInCart(cart, item);
        }

        String redirectLinkTemplate = "redirect:/items?search=%s&sort=%s&pageNumber=%d&pageSize=%d";
        return redirectLinkTemplate.formatted(search, sortType, pageNumber, pageSize);
    }

    @PostMapping(value = "/items/{id}")
    public String changeItemQuantityInCartFromItemInCartPage(
            @PathVariable Long id,
            @RequestParam String action,
            Model model) {

        ActionType actionType = ActionType.valueOf(action.toUpperCase());

        Item item = itemService.getItemById(id);
        Cart cart = cartService.getOrCreate();

        switch (actionType) {
            case MINUS -> cartService.decreaseItemQuantityInCart(cart, item);
            case PLUS -> cartService.increaseItemQuantityInCart(cart, item);
        }

        model.addAttribute("item", ItemDto.fromItem(itemService.getItemById(id)));
        return "item";
    }
}
