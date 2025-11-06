package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void findByBookerId_ShouldReturnUserBookings() {
        User owner = new User(null, "Owner", "owner@example.com");
        User booker = new User(null, "Booker", "booker@example.com");
        User savedOwner = entityManager.persistAndFlush(owner);
        User savedBooker = entityManager.persistAndFlush(booker);

        Item item = new Item(null, "Drill", "Powerful drill", true, savedOwner, null);
        Item savedItem = entityManager.persistAndFlush(item);

        Booking booking = new Booking(null,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                savedItem, savedBooker, BookingStatus.WAITING);
        entityManager.persistAndFlush(booking);

        List<Booking> bookings = bookingRepository.findByBookerIdOrderByStartDesc(
                savedBooker.getId(), PageRequest.of(0, 10));

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getItem().getName()).isEqualTo("Drill");
        assertThat(bookings.get(0).getBooker().getName()).isEqualTo("Booker");
    }

    @Test
    void findByItemOwnerId_ShouldReturnOwnerBookings() {
        User owner = new User(null, "Owner", "owner@example.com");
        User booker = new User(null, "Booker", "booker@example.com");
        User savedOwner = entityManager.persistAndFlush(owner);
        User savedBooker = entityManager.persistAndFlush(booker);

        Item item = new Item(null, "Drill", "Powerful drill", true, savedOwner, null);
        Item savedItem = entityManager.persistAndFlush(item);

        Booking booking = new Booking(null,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                savedItem, savedBooker, BookingStatus.WAITING);
        entityManager.persistAndFlush(booking);

        List<Booking> bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(
                savedOwner.getId(), PageRequest.of(0, 10));

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getItem().getName()).isEqualTo("Drill");
    }

    @Test
    void findFirstByItemIdAndStartLessThanEqual_ShouldReturnLastBooking() {
        User owner = new User(null, "Owner", "owner@example.com");
        User booker = new User(null, "Booker", "booker@example.com");
        User savedOwner = entityManager.persistAndFlush(owner);
        User savedBooker = entityManager.persistAndFlush(booker);

        Item item = new Item(null, "Drill", "Powerful drill", true, savedOwner, null);
        Item savedItem = entityManager.persistAndFlush(item);

        Booking booking = new Booking(null,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                savedItem, savedBooker, BookingStatus.APPROVED);
        entityManager.persistAndFlush(booking);

        LocalDateTime now = LocalDateTime.now();
        var result = bookingRepository.findFirstByItemIdAndStartLessThanEqualAndStatusOrderByStartDesc(
                savedItem.getId(), now, BookingStatus.APPROVED);

        assertThat(result).isPresent();
        assertThat(result.get().getItem().getName()).isEqualTo("Drill");
    }
}