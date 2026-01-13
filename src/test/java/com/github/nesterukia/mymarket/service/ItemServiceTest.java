package com.github.nesterukia.mymarket.service;

import com.github.nesterukia.mymarket.dao.ItemRepository;
import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.domain.SortType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @Test
    void getItems_NoSearchNoSort_ReturnsPageWithUnsortedPageable() {
        Page<Item> mockPage = new PageImpl<>(List.of());
        PageRequest expectedPageable = PageRequest.of(0, 5);
        when(itemRepository.findByTitleOrDescriptionContainingIgnoreCase("", expectedPageable))
                .thenReturn(mockPage);

        Page<Item> result = itemService.getItems("", SortType.NO, 1, 5);

        assertThat(result).isEqualTo(mockPage);
        verify(itemRepository).findByTitleOrDescriptionContainingIgnoreCase("", expectedPageable);
    }

    @Test
    void getItems_WithSearch_ReturnsPageWithSearchTerm() {
        Page<Item> mockPage = new PageImpl<>(List.of());
        PageRequest expectedPageable = PageRequest.of(0, 10);
        when(itemRepository.findByTitleOrDescriptionContainingIgnoreCase("phone", expectedPageable))
                .thenReturn(mockPage);

        Page<Item> result = itemService.getItems("phone", SortType.NO, 1, 10);

        verify(itemRepository).findByTitleOrDescriptionContainingIgnoreCase("phone", expectedPageable);
    }

    @Test
    void getItems_AlphaSort_ReturnsPageWithTitleAscSort() {
        Page<Item> mockPage = new PageImpl<>(List.of());
        PageRequest expectedPageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "title"));
        when(itemRepository.findByTitleOrDescriptionContainingIgnoreCase("", expectedPageable))
                .thenReturn(mockPage);

        Page<Item> result = itemService.getItems("", SortType.ALPHA, 1, 5);

        verify(itemRepository).findByTitleOrDescriptionContainingIgnoreCase("", expectedPageable);
    }

    @Test
    void getItems_PriceSort_ReturnsPageWithPriceAscSort() {
        Page<Item> mockPage = new PageImpl<>(List.of());
        PageRequest expectedPageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "price"));
        when(itemRepository.findByTitleOrDescriptionContainingIgnoreCase("", expectedPageable))
                .thenReturn(mockPage);

        Page<Item> result = itemService.getItems("", SortType.PRICE, 1, 5);

        verify(itemRepository).findByTitleOrDescriptionContainingIgnoreCase("", expectedPageable);
    }

    @Test
    void getItems_PageNumber2_ReturnsPageWithOffset4() {
        Page<Item> mockPage = new PageImpl<>(List.of());
        PageRequest expectedPageable = PageRequest.of(1, 5);  // pageNumber-1
        when(itemRepository.findByTitleOrDescriptionContainingIgnoreCase("", expectedPageable))
                .thenReturn(mockPage);

        Page<Item> result = itemService.getItems("", SortType.NO, 2, 5);

        verify(itemRepository).findByTitleOrDescriptionContainingIgnoreCase("", expectedPageable);
    }

    @Test
    void getItemById_ExistingItem_ReturnsItem() {
        Long id = 1L;
        Item mockItem = new Item();
        when(itemRepository.findById(id)).thenReturn(Optional.of(mockItem));

        Item result = itemService.getItemById(id);

        assertThat(result).isEqualTo(mockItem);
        verify(itemRepository).findById(id);
    }

    @Test
    void getItemById_NonExistingItem_ThrowsException() {
        Long id = 999L;
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItemById(id))
                .isInstanceOf(RuntimeException.class);  // orElseThrow()

        verify(itemRepository).findById(id);
    }

    @Test
    void getItems_AllParamsCombined_CorrectPageable() {
        Page<Item> mockPage = new PageImpl<>(List.of());
        PageRequest expectedPageable = PageRequest.of(2, 20, Sort.by(Sort.Direction.ASC, "price"));  // page 3-1
        when(itemRepository.findByTitleOrDescriptionContainingIgnoreCase("laptop", expectedPageable))
                .thenReturn(mockPage);

        Page<Item> result = itemService.getItems("laptop", SortType.PRICE, 3, 20);

        verify(itemRepository).findByTitleOrDescriptionContainingIgnoreCase("laptop", expectedPageable);
    }

    @Test
    void getItems_NegativePageNumber_UsesZeroOffset() {
        Page<Item> mockPage = new PageImpl<>(List.of());
        PageRequest expectedPageable = PageRequest.of(0, 5);
        when(itemRepository.findByTitleOrDescriptionContainingIgnoreCase("", expectedPageable))
                .thenReturn(mockPage);

        Page<Item> result = itemService.getItems("", SortType.NO, 1, 5);

        verify(itemRepository).findByTitleOrDescriptionContainingIgnoreCase("", expectedPageable);
    }
}