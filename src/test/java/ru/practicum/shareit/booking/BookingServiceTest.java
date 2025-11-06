package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    private BookingService bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(bookingRepository, itemRepository, userRepository);

        owner = new User(1L, "Owner", "owner@example.com");
        booker = new User(2L, "Booker", "booker@example.com");
        item = new Item(1L, "Drill", "Powerful drill", true, owner, null);
        booking = new Booking(1L, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), item, booker, BookingStatus.WAITING);
    }

    @Test
    void createBooking_WhenValidData_ShouldCreateBooking() {
        BookingRequestDto bookingDto = new BookingRequestDto(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.createBooking(bookingDto, 2L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_WhenUserIsOwner_ShouldThrowException() {
        BookingRequestDto bookingDto = new BookingRequestDto(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(bookingDto, 1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Владелец не может бронировать свою вещь");
    }

    @Test
    void createBooking_WhenItemNotAvailable_ShouldThrowException() {
        Item unavailableItem = new Item(1L, "Drill", "Powerful drill", false, owner, null);
        BookingRequestDto bookingDto = new BookingRequestDto(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(unavailableItem));

        assertThatThrownBy(() -> bookingService.createBooking(bookingDto, 2L))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Вещь недоступна для бронирования");
    }

    @Test
    void createBooking_WhenInvalidDates_ShouldThrowException() {
        BookingRequestDto bookingDto = new BookingRequestDto(1L,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1));

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(bookingDto, 2L))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Дата начала не может быть позже даты окончания");
    }

    @Test
    void updateBookingStatus_WhenApproved_ShouldUpdateStatus() {
        Booking waitingBooking = new Booking(1L, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), item, booker, BookingStatus.WAITING);
        Booking approvedBooking = new Booking(1L, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), item, booker, BookingStatus.APPROVED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(waitingBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(approvedBooking);

        BookingResponseDto result = bookingService.updateBookingStatus(1L, true, 1L);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void updateBookingStatus_WhenNotOwner_ShouldThrowException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateBookingStatus(1L, true, 3L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Подтверждать бронирование может только владелец вещи");
    }

    @Test
    void getBookingById_WhenAuthor_ShouldReturnBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getBookingById(1L, 2L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getBookingById_WhenNotAuthorOrOwner_ShouldThrowException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getBookingById(1L, 3L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Просматривать бронирование может только автор или владелец вещи");
    }

    @Test
    void getUserBookings_WithAllState_ShouldReturnAllBookings() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findByBookerIdOrderByStartDesc(eq(2L), any(PageRequest.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(BookingState.ALL, 2L, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getOwnerBookings_WithAllState_ShouldReturnAllBookings() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(BookingState.ALL, 1L, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }
}