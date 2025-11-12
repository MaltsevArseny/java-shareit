package ru.practicum.shareit.server.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.booking.dto.BookingDto;
import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.item.service.ItemService;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private User owner;
    private User booker;
    private Long itemId;

    @BeforeEach
    void setUp() {
        // Создаем владельца
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@email.com");
        owner = userService.create(owner);

        // Создаем бронирующего
        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@email.com");
        booker = userService.create(booker);

        // Создаем вещь
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);
        ItemDto createdItem = itemService.create(itemDto, owner.getId());
        itemId = createdItem.getId();
    }

    @Test
    void create_shouldCreateBookingWithValidData() {
        // Given
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        // When
        BookingDto createdBooking = bookingService.create(bookingDto, booker.getId());

        // Then
        assertNotNull(createdBooking);
        assertNotNull(createdBooking.getId());
        assertEquals("WAITING", createdBooking.getStatus());
        assertEquals(itemId, createdBooking.getItemId());
        assertTrue(createdBooking.getStart().isAfter(LocalDateTime.now()));
        assertTrue(createdBooking.getEnd().isAfter(createdBooking.getStart()));
    }

    @Test
    void create_shouldThrowExceptionWhenItemNotAvailable() {
        // Given
        ItemDto unavailableItem = new ItemDto();
        unavailableItem.setName("Unavailable Item");
        unavailableItem.setDescription("Unavailable Description");
        unavailableItem.setAvailable(false);
        ItemDto createdItem = itemService.create(unavailableItem, owner.getId());

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(createdItem.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.create(bookingDto, booker.getId()));
        assertEquals("Item is not available for booking", exception.getMessage());
    }

    @Test
    void create_shouldThrowExceptionWhenOwnerBooksOwnItem() {
        // Given
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.create(bookingDto, owner.getId()));
        assertEquals("Owner cannot book their own item", exception.getMessage());
    }

    @Test
    void create_shouldThrowExceptionWhenEndDateBeforeStartDate() {
        // Given
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(2));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.create(bookingDto, booker.getId()));
        assertEquals("End date cannot be before start date", exception.getMessage());
    }

    @Test
    void create_shouldThrowExceptionWhenStartDateInPast() {
        // Given
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().minusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.create(bookingDto, booker.getId()));
        assertEquals("Start date cannot be in the past", exception.getMessage());
    }

    @Test
    void approve_shouldApproveBooking() {
        // Given
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        BookingDto createdBooking = bookingService.create(bookingDto, booker.getId());

        // When
        BookingDto approvedBooking = bookingService.approve(createdBooking.getId(), true, owner.getId());

        // Then
        assertEquals("APPROVED", approvedBooking.getStatus());
        assertEquals(createdBooking.getId(), approvedBooking.getId());
    }

    @Test
    void approve_shouldRejectBooking() {
        // Given
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        BookingDto createdBooking = bookingService.create(bookingDto, booker.getId());

        // When
        BookingDto rejectedBooking = bookingService.approve(createdBooking.getId(), false, owner.getId());

        // Then
        assertEquals("REJECTED", rejectedBooking.getStatus());
        assertEquals(createdBooking.getId(), rejectedBooking.getId());
    }

    @Test
    void approve_shouldThrowExceptionWhenNotOwner() {
        // Given
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        BookingDto createdBooking = bookingService.create(bookingDto, booker.getId());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.approve(createdBooking.getId(), true, booker.getId()));
        assertEquals("Only owner can approve booking", exception.getMessage());
    }

    @Test
    void approve_shouldThrowExceptionWhenBookingAlreadyProcessed() {
        // Given
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        BookingDto createdBooking = bookingService.create(bookingDto, booker.getId());
        bookingService.approve(createdBooking.getId(), true, owner.getId());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.approve(createdBooking.getId(), false, owner.getId()));
        assertEquals("Booking already processed", exception.getMessage());
    }

    @Test
    void getById_shouldReturnBookingForBooker() {
        // Given
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        BookingDto createdBooking = bookingService.create(bookingDto, booker.getId());

        // When
        BookingDto foundBooking = bookingService.getById(createdBooking.getId(), booker.getId());

        // Then
        assertNotNull(foundBooking);
        assertEquals(createdBooking.getId(), foundBooking.getId());
        assertEquals(itemId, foundBooking.getItemId());
    }

    @Test
    void getById_shouldReturnBookingForOwner() {
        // Given
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        BookingDto createdBooking = bookingService.create(bookingDto, booker.getId());

        // When
        BookingDto foundBooking = bookingService.getById(createdBooking.getId(), owner.getId());

        // Then
        assertNotNull(foundBooking);
        assertEquals(createdBooking.getId(), foundBooking.getId());
        assertEquals(itemId, foundBooking.getItemId());
    }

    @Test
    void getById_shouldThrowExceptionWhenAccessDenied() {
        // Given
        User otherUser = new User();
        otherUser.setName("Other User");
        otherUser.setEmail("other@email.com");
        otherUser = userService.create(otherUser);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        BookingDto createdBooking = bookingService.create(bookingDto, booker.getId());

        // When & Then
        User finalOtherUser = otherUser;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.getById(createdBooking.getId(), finalOtherUser.getId()));
        assertEquals("Access denied", exception.getMessage());
    }

    @Test
    void getAllByBooker_shouldReturnBookings() {
        // Given
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingService.create(bookingDto, booker.getId());

        // When
        List<BookingDto> bookings = bookingService.getAllByBooker(booker.getId(), "ALL", 0, 10);

        // Then
        assertFalse(bookings.isEmpty());
        assertEquals(1, bookings.size());
        assertEquals(itemId, bookings.getFirst().getItemId());
    }

    @Test
    void getAllByOwner_shouldReturnBookings() {
        // Given
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingService.create(bookingDto, booker.getId());

        // When
        List<BookingDto> bookings = bookingService.getAllByOwner(owner.getId(), "ALL", 0, 10);

        // Then
        assertFalse(bookings.isEmpty());
        assertEquals(1, bookings.size());
        assertEquals(itemId, bookings.getFirst().getItemId());
    }

    @Test
    void getAllByBooker_shouldHandleDifferentStates() {
        // Given
        BookingDto futureBooking = new BookingDto();
        futureBooking.setItemId(itemId);
        futureBooking.setStart(LocalDateTime.now().plusDays(1));
        futureBooking.setEnd(LocalDateTime.now().plusDays(2));
        bookingService.create(futureBooking, booker.getId());

        // When & Then - FUTURE state
        List<BookingDto> futureBookings = bookingService.getAllByBooker(booker.getId(), "FUTURE", 0, 10);
        assertFalse(futureBookings.isEmpty());

        // When & Then - WAITING state
        List<BookingDto> waitingBookings = bookingService.getAllByBooker(booker.getId(), "WAITING", 0, 10);
        assertFalse(waitingBookings.isEmpty());
    }

    @Test
    void getAllByBooker_shouldThrowExceptionForInvalidPagination() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> bookingService.getAllByBooker(booker.getId(), "ALL", -1, 10));
        assertThrows(IllegalArgumentException.class,
                () -> bookingService.getAllByBooker(booker.getId(), "ALL", 0, 0));
    }

    @Test
    void getAllByBooker_shouldThrowExceptionForUnknownState() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> bookingService.getAllByBooker(booker.getId(), "UNKNOWN_STATE", 0, 10));
    }
}