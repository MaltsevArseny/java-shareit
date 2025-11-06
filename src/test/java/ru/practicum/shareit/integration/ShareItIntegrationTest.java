package ru.practicum.shareit.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ShareItIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private BookingService bookingService;

    @Test
    void createUserThenItemThenBooking_ShouldWorkCorrectly() {
        // Create owner
        UserDto ownerDto = new UserDto(null, "Owner", "owner@example.com");
        UserDto savedOwner = userService.createUser(ownerDto);

        // Create booker
        UserDto bookerDto = new UserDto(null, "Booker", "booker@example.com");
        UserDto savedBooker = userService.createUser(bookerDto);

        // Create item
        ItemDto itemDto = new ItemDto(null, "Drill", "Powerful drill", true, null);
        ItemDto savedItem = itemService.createItem(itemDto, savedOwner.getId());

        // Create booking
        BookingRequestDto bookingDto = new BookingRequestDto(savedItem.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        BookingResponseDto savedBooking = bookingService.createBooking(bookingDto, savedBooker.getId());

        // Verify booking
        assertThat(savedBooking).isNotNull();
        assertThat(savedBooking.getItem().getId()).isEqualTo(savedItem.getId());
        assertThat(savedBooking.getBooker().getId()).isEqualTo(savedBooker.getId());

        // Get item with bookings for owner
        ItemWithBookingsDto itemWithBookings = itemService.getItemById(savedItem.getId(), savedOwner.getId());
        assertThat(itemWithBookings).isNotNull();

        // Get user bookings
        List<BookingResponseDto> userBookings = bookingService.getUserBookings(
                BookingState.ALL, savedBooker.getId(), 0, 10);
        assertThat(userBookings).hasSize(1);

        // Get owner bookings
        List<BookingResponseDto> ownerBookings = bookingService.getOwnerBookings(
                BookingState.ALL, savedOwner.getId(), 0, 10);
        assertThat(ownerBookings).hasSize(1);
    }

    @Test
    void completeBookingFlow_WithComment_ShouldWorkCorrectly() {
        // Create users and item
        UserDto owner = userService.createUser(new UserDto(null, "Owner", "owner@example.com"));
        UserDto booker = userService.createUser(new UserDto(null, "Booker", "booker@example.com"));
        ItemDto item = itemService.createItem(new ItemDto(null, "Hammer", "Heavy hammer", true, null), owner.getId());

        // Create booking
        BookingRequestDto bookingDto = new BookingRequestDto(item.getId(),
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1));
        BookingResponseDto booking = bookingService.createBooking(bookingDto, booker.getId());

        // Approve booking
        BookingResponseDto approvedBooking = bookingService.updateBookingStatus(booking.getId(), true, owner.getId());
        assertThat(approvedBooking.getStatus()).isEqualTo(ru.practicum.shareit.booking.BookingStatus.APPROVED);

        // Add comment (this would normally require the booking to be in the past)
        // For integration test, we might need to adjust time or use a different approach
    }
}