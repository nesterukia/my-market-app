package com.github.nesterukia.mymarket.http;

import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.domain.SortType;
import com.github.nesterukia.mymarket.http.models.ItemDto;
import com.github.nesterukia.mymarket.http.models.ItemsDto;
import com.github.nesterukia.mymarket.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping(value = {"/items", "/"})
    public String getItems(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") String sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize,
            Model model) {

        SortType sortType = SortType.valueOf(sort.toUpperCase());
        Page<Item> pageOfItems = itemService.getItems(
                search,
                sortType,
                pageNumber,
                pageSize
        );

        ItemsDto itemsForTemplate = new ItemsDto(
                pageOfItems,
                sortType,
                search
        );

        model.addAttribute("items", itemsForTemplate.getItems());
        model.addAttribute("search", itemsForTemplate.getSearch());
        model.addAttribute("sort", itemsForTemplate.getSort());
        model.addAttribute("paging", itemsForTemplate.getPaging());
        return "items";
    }

    @GetMapping(value = "/items/{id}")
    public String getItemById(@PathVariable Long id, Model model) {
        Item itemById = itemService.getItemById(id);
        model.addAttribute("item", ItemDto.fromItem(itemById));
        return "item";
    }
}
