package com.github.nesterukia.mymarket.dao;

import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.utils.PostgresContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ItemRepositoryTest extends PostgresContainerTest {
    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    public void clearRepository(){
        itemRepository.deleteAll();
    }

    @Test
    public void testFindNoMatchesEmptySearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> result = itemRepository.findByTitleOrDescriptionContainingIgnoreCase("", pageable);
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    public void testFindNoMatchesNonExistentSearch() {
        Item item = new Item();
        item.setTitle("Apple Product");
        item.setDescription("iPhone description");
        itemRepository.save(item);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> result = itemRepository.findByTitleOrDescriptionContainingIgnoreCase("banana", pageable);
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    public void testFindByTitleMatchCaseInsensitive() {
        Item item = new Item();
        item.setTitle("APPLE PRODUCT");
        item.setDescription("description");
        itemRepository.save(item);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> result = itemRepository.findByTitleOrDescriptionContainingIgnoreCase("apple", pageable);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("APPLE PRODUCT");
    }

    @Test
    public void testFindByDescriptionMatchCaseInsensitive() {
        Item item = new Item();
        item.setTitle("title");
        item.setDescription("DESCRIPTION WITH PHONE");
        itemRepository.save(item);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> result = itemRepository.findByTitleOrDescriptionContainingIgnoreCase("phone", pageable);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getDescription()).isEqualTo("DESCRIPTION WITH PHONE");
    }

    @Test
    public void testFindPaginationMultipleItems() {
        Item item1 = new Item();
        item1.setTitle("First Item");
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setTitle("Second Item");
        itemRepository.save(item2);

        Pageable pageable = PageRequest.of(0, 1, Sort.by("title"));
        Page<Item> result = itemRepository.findByTitleOrDescriptionContainingIgnoreCase("item", pageable);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("First Item");
    }
}

