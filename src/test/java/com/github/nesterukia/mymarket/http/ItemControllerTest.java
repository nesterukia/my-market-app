package com.github.nesterukia.mymarket.http;

import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.domain.SortType;
import com.github.nesterukia.mymarket.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ItemService itemService;

    @Test
    void getItems_DefaultParams_CallsServiceAndReturnsItemsView() throws Exception {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Item> mockPage = new PageImpl<>(List.of(), pageable, 0);
        when(itemService.getItems("", SortType.NO, 1, 5)).thenReturn(mockPage);

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"));

        verify(itemService).getItems("", SortType.NO, 1, 5);
    }

    @Test
    void getItems_WithSearchParam_CallsServiceWithSearch() throws Exception {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Item> mockPage = new PageImpl<>(List.of(), pageable, 0);
        when(itemService.getItems("phone", SortType.NO, 1, 5)).thenReturn(mockPage);

        mockMvc.perform(get("/items").param("search", "phone"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"));

        verify(itemService).getItems("phone", SortType.NO, 1, 5);
    }

    @Test
    void getItems_WithSortParam_CallsServiceWithSortType() throws Exception {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Item> mockPage = new PageImpl<>(List.of(), pageable, 0);
        when(itemService.getItems("", SortType.PRICE, 1, 5)).thenReturn(mockPage);

        mockMvc.perform(get("/items").param("sort", "price"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"));

        verify(itemService).getItems("", SortType.PRICE, 1, 5);
    }

    @Test
    void getItems_WithPageNumberParam_CallsServiceWithPageNumber() throws Exception {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Item> mockPage = new PageImpl<>(List.of(), pageable, 0);
        when(itemService.getItems("", SortType.NO, 2, 5)).thenReturn(mockPage);

        mockMvc.perform(get("/items").param("pageNumber", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"));

        verify(itemService).getItems("", SortType.NO, 2, 5);
    }

    @Test
    void getItems_WithPageSizeParam_CallsServiceWithPageSize() throws Exception {
        Pageable pageable = PageRequest.of(0, 5);
    Page<Item> mockPage = new PageImpl<>(List.of(), pageable, 0);
        when(itemService.getItems("", SortType.NO, 1, 10)).thenReturn(mockPage);

        mockMvc.perform(get("/items").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"));

        verify(itemService).getItems("", SortType.NO, 1, 10);
    }

    @Test
    void getItems_RootPath_CallsServiceAndReturnsItemsView() throws Exception {
        Pageable pageable = PageRequest.of(0, 5);
    Page<Item> mockPage = new PageImpl<>(List.of(), pageable, 0);
        when(itemService.getItems("", SortType.NO, 1, 5)).thenReturn(mockPage);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"));

        verify(itemService).getItems("", SortType.NO, 1, 5);
    }

    @Test
    void getItemById_ValidId_CallsServiceAndReturnsItemView() throws Exception {
        Item mockItem = new Item();
        when(itemService.getItemById(1L)).thenReturn(mockItem);

        mockMvc.perform(get("/items/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("item"));

        verify(itemService).getItemById(1L);
    }
}
