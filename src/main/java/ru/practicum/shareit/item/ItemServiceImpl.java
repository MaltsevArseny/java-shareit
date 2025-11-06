package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID " + userId + " не найден"));

        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(owner);
        item.setRequestId(itemDto.getRequestId());

        Item savedItem = itemRepository.save(item);
        return toDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(ItemDto itemDto, Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Вещь с ID " + itemId + " не найдена"));

        if (!item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Редактировать вещь может только её владелец");
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(item);
        return toDto(updatedItem);
    }

    @Override
    public ItemWithBookingsDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Вещь с ID " + itemId + " не найдена"));

        List<CommentResponseDto> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId)
                .stream()
                .map(this::toCommentDto)
                .collect(Collectors.toList());

        ItemWithBookingsDto itemWithBookingsDto = toItemWithBookingsDto(item);
        itemWithBookingsDto.setComments(comments);

        // Добавляем информацию о бронированиях только для владельца
        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            // Последнее бронирование
            Booking lastBooking = bookingRepository
                    .findFirstByItemIdAndStartLessThanEqualAndStatusOrderByStartDesc(
                            itemId, now, BookingStatus.APPROVED)
                    .orElse(null);

            // Следующее бронирование
            Booking nextBooking = bookingRepository
                    .findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                            itemId, now, BookingStatus.APPROVED)
                    .orElse(null);

            if (lastBooking != null) {
                itemWithBookingsDto.setLastBooking(
                        new ItemWithBookingsDto.BookingInfo(lastBooking.getId(), lastBooking.getBooker().getId()));
            }
            if (nextBooking != null) {
                itemWithBookingsDto.setNextBooking(
                        new ItemWithBookingsDto.BookingInfo(nextBooking.getId(), nextBooking.getBooker().getId()));
            }
        }

        return itemWithBookingsDto;
    }

    @Override
    public List<ItemWithBookingsDto> getUserItems(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Item> items = itemRepository.findByOwnerIdOrderById(userId, pageable);

        return items.stream()
                .map(item -> getItemById(item.getId(), userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text, int from, int size) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        Pageable pageable = PageRequest.of(from / size, size);
        return itemRepository.searchAvailableItems(text, pageable)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponseDto addComment(Long itemId, CommentRequestDto commentDto, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Вещь с ID " + itemId + " не найдена"));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID " + userId + " не найден"));

        // Проверяем, что пользователь действительно брал вещь в аренду
        List<Booking> userBookings = bookingRepository.findCompletedBookingsByUserAndItem(
                userId, itemId, BookingStatus.APPROVED, LocalDateTime.now());

        if (userBookings.isEmpty()) {
            throw new ValidationException("Пользователь не брал эту вещь в аренду или аренда еще не завершена");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return toCommentDto(savedComment);
    }

    private ItemDto toDto(Item item) {
        return new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getAvailable(), item.getRequestId());
    }

    private ItemWithBookingsDto toItemWithBookingsDto(Item item) {
        return new ItemWithBookingsDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                null,
                null,
                Collections.emptyList()
        );
    }

    private CommentResponseDto toCommentDto(Comment comment) {
        return new CommentResponseDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }
}