package com.github.nesterukia.mymarket.dao;

import com.github.nesterukia.mymarket.domain.Item;
import com.github.nesterukia.mymarket.utils.CachedDbContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

@SpringBootTest
public class ItemRepositoryTest extends CachedDbContainerTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll().block();
        itemRepository.deleteAll().block();
    }

    @Test
    void findByTitleOrDescriptionContainingIgnoreCase_ShouldReturnMatchingItems() {
        Item item1 = Item.builder()
                .title("Smartphone iPhone 13")
                .description("Latest Apple smartphone with A15 chip")
                .imgPath("/images/iphone13.jpg")
                .price(999.0)
                .build();

        Item item2 = Item.builder()
                .title("Samsung Galaxy S21")
                .description("Android smartphone with great camera")
                .imgPath("/images/samsung21.jpg")
                .price(899.0)
                .build();

        Item item3 = Item.builder()
                .title("MacBook Pro")
                .description("Powerful laptop for developers")
                .imgPath("/images/macbook.jpg")
                .price(1999.0)
                .build();

        Flux<Item> saveAll = itemRepository.saveAll(List.of(item1, item2, item3));

        StepVerifier.create(saveAll.thenMany(itemRepository.findByTitleOrDescriptionContainingIgnoreCase("smartphone", PageRequest.of(0, 10))))
                .expectNextCount(2)
                .verifyComplete();

        StepVerifier.create(saveAll.thenMany(itemRepository.findByTitleOrDescriptionContainingIgnoreCase("iphone", PageRequest.of(0, 10))))
                .expectNextMatches(item -> item.getTitle().equals("Smartphone iPhone 13"))
                .verifyComplete();

        StepVerifier.create(saveAll.thenMany(itemRepository.findByTitleOrDescriptionContainingIgnoreCase("laptop", PageRequest.of(0, 10))))
                .expectNextMatches(item -> item.getTitle().equals("MacBook Pro"))
                .verifyComplete();
    }

    @Test
    void findByTitleOrDescriptionContainingIgnoreCase_ShouldReturnEmptyFluxWhenNoMatches() {
        Item item = Item.builder()
                .title("Test Item")
                .description("Test Description")
                .imgPath("/images/test.jpg")
                .price(100.0)
                .build();

        Mono<Item> saveItem = itemRepository.save(item);

        StepVerifier.create(saveItem.thenMany(itemRepository.findByTitleOrDescriptionContainingIgnoreCase("nonexistent", PageRequest.of(0, 10))))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void findByTitleOrDescriptionContainingIgnoreCase_ShouldBeCaseInsensitive() {
        Item item = Item.builder()
                .title("GAMING LAPTOP")
                .description("HIGH PERFORMANCE FOR GAMES")
                .imgPath("/images/gaming.jpg")
                .price(1500.0)
                .build();

        Mono<Item> saveItem = itemRepository.save(item);

        StepVerifier.create(saveItem.thenMany(itemRepository.findByTitleOrDescriptionContainingIgnoreCase("games", PageRequest.of(0, 10))))
                .expectNextMatches(i -> i.getTitle().equals("GAMING LAPTOP"))
                .verifyComplete();

        StepVerifier.create(itemRepository.findByTitleOrDescriptionContainingIgnoreCase("GAMES", PageRequest.of(0, 10)))
                .expectNextMatches(i -> i.getTitle().equals("GAMING LAPTOP"))
                .verifyComplete();
    }

    @Test
    void countByTitleOrDescriptionContainingIgnoreCase_ShouldReturnCorrectCount() {
        Item item1 = Item.builder()
                .title("Wireless Mouse")
                .description("Ergonomic wireless mouse for work")
                .imgPath("/images/mouse.jpg")
                .price(50.0)
                .build();

        Item item2 = Item.builder()
                .title("Mechanical Keyboard")
                .description("RGB mechanical keyboard for gaming")
                .imgPath("/images/keyboard.jpg")
                .price(120.0)
                .build();

        Item item3 = Item.builder()
                .title("Mouse Pad")
                .description("Large gaming mouse pad")
                .imgPath("/images/mousepad.jpg")
                .price(25.0)
                .build();

        Flux<Item> saveAll = itemRepository.saveAll(List.of(item1, item2, item3));

        StepVerifier.create(saveAll.then(itemRepository.countByTitleOrDescriptionContainingIgnoreCase("mouse")))
                .expectNext(2L)
                .verifyComplete();

        StepVerifier.create(saveAll.then(itemRepository.countByTitleOrDescriptionContainingIgnoreCase("gaming")))
                .expectNext(2L)
                .verifyComplete();

        StepVerifier.create(saveAll.then(itemRepository.countByTitleOrDescriptionContainingIgnoreCase("nonexistent")))
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    void findByTitleOrDescriptionContainingIgnoreCase_ShouldRespectPagination() {
        Item item1 = Item.builder().title("Product A").description("First product").imgPath("/img/a.jpg").price(10.0).build();
        Item item2 = Item.builder().title("Product B").description("Second product").imgPath("/img/b.jpg").price(20.0).build();
        Item item3 = Item.builder().title("Product C").description("Third product").imgPath("/img/c.jpg").price(30.0).build();
        Item item4 = Item.builder().title("Product D").description("Fourth product").imgPath("/img/d.jpg").price(40.0).build();
        Item item5 = Item.builder().title("Product E").description("Fifth product").imgPath("/img/e.jpg").price(50.0).build();

        Flux<Item> saveAll = itemRepository.saveAll(List.of(item1, item2, item3, item4, item5));

        StepVerifier.create(saveAll.thenMany(itemRepository.findByTitleOrDescriptionContainingIgnoreCase("product", PageRequest.of(0, 2))))
                .expectNextCount(5)
                .verifyComplete();
    }
}