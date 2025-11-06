package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.user.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void findByOwnerId_ShouldReturnUserItems() {
        User owner = new User(null, "Owner", "owner@example.com");
        User savedOwner = entityManager.persistAndFlush(owner);

        Item item1 = new Item(null, "Item 1", "Description 1", true, savedOwner, null);
        Item item2 = new Item(null, "Item 2", "Description 2", true, savedOwner, null);
        entityManager.persistAndFlush(item1);
        entityManager.persistAndFlush(item2);

        List<Item> items = itemRepository.findByOwnerIdOrderById(savedOwner.getId(), PageRequest.of(0, 10));

        assertThat(items).hasSize(2);
        assertThat(items).extracting(Item::getName)
                .containsExactly("Item 1", "Item 2");
    }

    @Test
    void searchAvailableItems_ShouldReturnMatchingItems() {
        User owner = new User(null, "Owner", "owner@example.com");
        User savedOwner = entityManager.persistAndFlush(owner);

        Item item1 = new Item(null, "Drill", "Powerful drill", true, savedOwner, null);
        Item item2 = new Item(null, "Hammer", "Heavy hammer", true, savedOwner, null);
        Item item3 = new Item(null, "Saw", "Broken saw", false, savedOwner, null);
        entityManager.persistAndFlush(item1);
        entityManager.persistAndFlush(item2);
        entityManager.persistAndFlush(item3);

        List<Item> items = itemRepository.searchAvailableItems("drill", PageRequest.of(0, 10));

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getName()).isEqualTo("Drill");
    }

    @Test
    void searchAvailableItems_WithEmptyText_ShouldReturnEmptyList() {
        User owner = new User(null, "Owner", "owner@example.com");
        User savedOwner = entityManager.persistAndFlush(owner);
        Item item = new Item(null, "Drill", "Powerful drill", true, savedOwner, null);
        entityManager.persistAndFlush(item);

        List<Item> items = itemRepository.searchAvailableItems("", PageRequest.of(0, 10));

        assertThat(items).isEmpty();
    }
}