package ru.practicum.shareit.server.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.booking.dto.BookingDto;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.booking.repository.BookingRepository;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.item.repository.ItemRepository;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDto create(BookingDto bookingDto, Long userId) {
        User booker = userService.getUserById(userId);
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Item is not available for booking");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Owner cannot book their own item");
        }

        if (bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        if (bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }

        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        return convertToDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingDto approve(Long bookingId, Boolean approved, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Only owner can approve booking");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalArgumentException("Booking already processed");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);
        return convertToDto(updatedBooking);
    }

    @Override
    public BookingDto getById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Access denied");
        }

        return convertToDto(booking);
    }

    @Override
    public List<BookingDto> getAllByBooker(Long userId, String state, Integer from, Integer size) {
        userService.getUserById(userId);
        Pageable pageable = PageRequest.of(from / size, size);

        List<Booking> bookings = switch (state.toUpperCase()) {
            case "ALL" -> bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable);
            case "CURRENT" -> bookingRepository.findCurrentBookingsByBooker(userId, LocalDateTime.now(), pageable);
            case "PAST" -> bookingRepository.findPastBookingsByBooker(userId, LocalDateTime.now(), pageable);
            case "FUTURE" -> bookingRepository.findFutureBookingsByBooker(userId, LocalDateTime.now(), pageable);
            case "WAITING", "REJECTED" ->
                    bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.valueOf(state.toUpperCase()), pageable);
            default -> throw new IllegalArgumentException("Unknown state: " + state);
        };

        return bookings.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllByOwner(Long userId, String state, Integer from, Integer size) {
        userService.getUserById(userId);
        Pageable pageable = PageRequest.of(from / size, size);

        List<Booking> bookings = switch (state.toUpperCase()) {
            case "ALL" -> bookingRepository.findByItemOwnerIdOrderByStartDesc(userId, pageable);
            case "CURRENT" -> bookingRepository.findCurrentBookingsByOwner(userId, LocalDateTime.now(), pageable);
            case "PAST" -> bookingRepository.findPastBookingsByOwner(userId, LocalDateTime.now(), pageable);
            case "FUTURE" -> bookingRepository.findFutureBookingsByOwner(userId, LocalDateTime.now(), pageable);
            case "WAITING", "REJECTED" ->
                    bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.valueOf(state.toUpperCase()), pageable);
            default -> throw new IllegalArgumentException("Unknown state: " + state);
        };

        return bookings.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    private BookingDto convertToDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getItem().getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus().name()
        );
    }
}