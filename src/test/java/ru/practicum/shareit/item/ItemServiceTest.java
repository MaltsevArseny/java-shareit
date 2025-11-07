package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
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
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    private ItemService itemService;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository);

        owner = new User(1L, "Owner", "owner@example.com");
        booker = new User(2L, "Booker", "booker@example.com");
        item = new Item(1L, "Drill", "Powerful drill", true, owner, null);
    }

    @Test
    void createItem_WhenUserExists_ShouldCreateItem() {
        ItemDto itemDto = new ItemDto(null, "Drill", "Powerful drill", true, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.createItem(itemDto, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Drill");
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void createItem_WhenUserNotExists_ShouldThrowException() {
        ItemDto itemDto = new ItemDto(null, "Drill", "Powerful drill", true, null);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.createItem(itemDto, 1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateItem_WhenUserIsOwner_ShouldUpdateItem() {
        ItemDto updateDto = new ItemDto(null, "Updated Drill", null, null, null);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.updateItem(updateDto, 1L, 1L);

        assertThat(result.getName()).isEqualTo("Drill");
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void updateItem_WhenUserNotOwner_ShouldThrowException() {
        ItemDto updateDto = new ItemDto(null, "Updated Drill", null, null, null);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.updateItem(updateDto, 1L, 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Редактировать вещь может только её владелец");
    }

    @Test
    void getItemById_WhenUserIsOwner_ShouldReturnItemWithBookings() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemIdOrderByCreatedDesc(1L)).thenReturn(List.of());

        ItemWithBookingsDto result = itemService.getItemById(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Drill");
    }

    @Test
    void getItemById_WhenUserNotOwner_ShouldReturnItemWithoutBookings() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemIdOrderByCreatedDesc(1L)).thenReturn(List.of());

        ItemWithBookingsDto result = itemService.getItemById(1L, 3L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Drill");
        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
    }

    @Test
    void searchItems_ShouldReturnAvailableItems() {
        when(itemRepository.searchAvailableItems(eq("drill"), any(PageRequest.class)))
                .thenReturn(List.of(item));

        List<ItemDto> result = itemService.searchItems("drill", 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Drill");
    }

    @Test
    void searchItems_WithBlankText_ShouldReturnEmptyList() {
        List<ItemDto> result = itemService.searchItems("", 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void searchItems_WithNullText_ShouldReturnEmptyList() {
        List<ItemDto> result = itemService.searchItems(null, 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void addComment_WhenUserHasBookings_ShouldAddComment() {
        CommentRequestDto commentDto = new CommentRequestDto("Great item!");
        Booking booking = new Booking(1L, LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1), item, booker, BookingStatus.APPROVED);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findCompletedBookingsByUserAndItem(eq(2L), eq(1L),
                eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(1L);
            comment.setText("Great item!");
            comment.setAuthor(booker);
            comment.setItem(item);
            comment.setCreated(LocalDateTime.now());
            return comment;
        });

        CommentResponseDto result = itemService.addComment(1L, commentDto, 2L);

        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("Great item!");
        assertThat(result.getAuthorName()).isEqualTo("Booker");
    }

    @Test
    void addComment_WhenUserNoBookings_ShouldThrowException() {
        CommentRequestDto commentDto = new CommentRequestDto("Great item!");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findCompletedBookingsByUserAndItem(eq(2L), eq(1L),
                eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> itemService.addComment(1L, commentDto, 2L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Пользователь не брал эту вещь в аренду");
    }

    @Test
    void addComment_WhenItemNotExists_ShouldThrowException() {
        CommentRequestDto commentDto = new CommentRequestDto("Great item!");

        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.addComment(1L, commentDto, 2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Вещь с ID 1 не найдена");
    }

    @Test
    void addComment_WhenUserNotExists_ShouldThrowException() {
        CommentRequestDto commentDto = new CommentRequestDto("Great item!");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.addComment(1L, commentDto, 2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Пользователь с ID 2 не найден");
    }

    @Test
    void getUserItems_ShouldReturnUserItems() {
        when(itemRepository.findByOwnerIdOrderById(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of(item));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemIdOrderByCreatedDesc(1L)).thenReturn(List.of());

        List<ItemWithBookingsDto> result = itemService.getUserItems(1L, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Drill");
    }
}