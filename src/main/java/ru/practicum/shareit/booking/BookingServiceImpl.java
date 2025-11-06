package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto bookingDto, Long userId) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID " + userId + " не найден"));

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("Вещь с ID " + bookingDto.getItemId() + " не найдена"));

        // Проверка, что пользователь не владелец вещи
        if (item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Владелец не может бронировать свою вещь");
        }

        // Проверка доступности вещи
        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        // Проверка дат
        if (bookingDto.getStart().isAfter(bookingDto.getEnd())) {
            throw new ValidationException("Дата начала не может быть позже даты окончания");
        }

        if (bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            throw new ValidationException("Дата начала и окончания не могут совпадать");
        }

        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        return toDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto updateBookingStatus(Long bookingId, Boolean approved, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Бронирование с ID " + bookingId + " не найдено"));

        // Проверка, что пользователь - владелец вещи
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Подтверждать бронирование может только владелец вещи");
        }

        // Проверка, что бронирование еще не подтверждено
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Статус бронирования уже изменен");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);
        return toDto(updatedBooking);
    }

    @Override
    public BookingResponseDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Бронирование с ID " + bookingId + " не найдено"));

        // Проверка прав доступа
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Просматривать бронирование может только автор или владелец вещи");
        }

        return toDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(BookingState state, Long userId, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Пользователь с ID " + userId + " не найден");
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case ALL:
                return bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable)
                        .stream()
                        .map(this::toDto)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                                userId, now, now, pageable)
                        .stream()
                        .map(this::toDto)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now, pageable)
                        .stream()
                        .map(this::toDto)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now, pageable)
                        .stream()
                        .map(this::toDto)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING, pageable)
                        .stream()
                        .map(this::toDto)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED, pageable)
                        .stream()
                        .map(this::toDto)
                        .collect(Collectors.toList());
            default:
                throw new ValidationException("Unknown state: " + state);
        }
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(BookingState state, Long userId, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Пользователь с ID " + userId + " не найден");
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case ALL:
                return bookingRepository.findByItemOwnerIdOrderByStartDesc(userId, pageable)
                        .stream()
                        .map(this::toDto)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                                userId, now, now, pageable)
                        .stream()
                        .map(this::toDto)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now, pageable)
                        .stream()
                        .map(this::toDto)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now, pageable)
                        .stream()
                        .map(this::toDto)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING, pageable)
                        .stream()
                        .map(this::toDto)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED, pageable)
                        .stream()
                        .map(this::toDto)
                        .collect(Collectors.toList());
            default:
                throw new ValidationException("Unknown state: " + state);
        }
    }

    private BookingResponseDto toDto(Booking booking) {
        BookingResponseDto.Booker booker = new BookingResponseDto.Booker(
                booking.getBooker().getId(),
                booking.getBooker().getName()
        );

        BookingResponseDto.Item item = new BookingResponseDto.Item(
                booking.getItem().getId(),
                booking.getItem().getName()
        );

        return new BookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                booker,
                item
        );
    }
}