package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void findByItemId_ShouldReturnCommentsForItem() {
        User owner = new User(null, "Owner", "owner@example.com");
        User author = new User(null, "Author", "author@example.com");
        User savedOwner = entityManager.persistAndFlush(owner);
        User savedAuthor = entityManager.persistAndFlush(author);

        Item item = new Item(null, "Drill", "Powerful drill", true, savedOwner, null);
        Item savedItem = entityManager.persistAndFlush(item);

        Comment comment1 = new Comment(null, "Great item!", savedItem, savedAuthor, LocalDateTime.now());
        Comment comment2 = new Comment(null, "Very useful", savedItem, savedAuthor, LocalDateTime.now().plusHours(1));
        entityManager.persistAndFlush(comment1);
        entityManager.persistAndFlush(comment2);

        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(savedItem.getId());

        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getText()).isEqualTo("Very useful"); // Should be ordered by created desc
        assertThat(comments.get(1).getText()).isEqualTo("Great item!");
    }

    @Test
    void findByItemIdIn_ShouldReturnCommentsForMultipleItems() {
        User owner = new User(null, "Owner", "owner@example.com");
        User author = new User(null, "Author", "author@example.com");
        User savedOwner = entityManager.persistAndFlush(owner);
        User savedAuthor = entityManager.persistAndFlush(author);

        Item item1 = new Item(null, "Drill", "Powerful drill", true, savedOwner, null);
        Item item2 = new Item(null, "Hammer", "Heavy hammer", true, savedOwner, null);
        Item savedItem1 = entityManager.persistAndFlush(item1);
        Item savedItem2 = entityManager.persistAndFlush(item2);

        Comment comment1 = new Comment(null, "Great drill!", savedItem1, savedAuthor, LocalDateTime.now());
        Comment comment2 = new Comment(null, "Good hammer", savedItem2, savedAuthor, LocalDateTime.now());
        entityManager.persistAndFlush(comment1);
        entityManager.persistAndFlush(comment2);

        List<Comment> comments = commentRepository.findByItemIdInOrderByCreatedDesc(
                List.of(savedItem1.getId(), savedItem2.getId()));

        assertThat(comments).hasSize(2);
        assertThat(comments).extracting(Comment::getText)
                .containsExactlyInAnyOrder("Great drill!", "Good hammer");
    }
}