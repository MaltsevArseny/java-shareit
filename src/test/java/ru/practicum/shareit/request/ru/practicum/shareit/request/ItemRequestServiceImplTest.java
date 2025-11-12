package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    @Mock
    private itemrequestrepository requestRepository;
    @Mock
    private UserService userService;
    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    @Test
    void createRequest_whenValid_thenSuccess() {
        // given
        ItemRequestDto requestDto = createRequestDto();
        User user = createUser(1L);
        ItemRequest request = createRequest(user);

        when(userService.getUserById(1L)).thenReturn(user);
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(request);

        // when
        ItemRequestDto result = requestService.createRequest(requestDto, 1L);

        // then
        assertNotNull(result);
        assertEquals("Need drill", result.getDescription());
        verify(requestRepository).save(any(ItemRequest.class));
    }

    @Test
    void getUserRequests_whenUserExists_thenReturnRequests() {
        // given
        User user = createUser(1L);
        ItemRequest request = createRequest(user);

        when(userService.validateUserExists(1L)).thenReturn(true);
        when(requestRepository.findAllByRequesterIdOrderByCreatedDesc(1L))
                .thenReturn(List.of(request));
        when(itemService.getItemsByRequestId(1L)).thenReturn(List.of());

        // when
        List<ItemRequestDto> result = requestService.getUserRequests(1L);

        // then
        assertEquals(1, result.size());
        assertEquals("Need drill", result.getFirst().getDescription());
    }

    @Test
    void getRequestById_whenRequestExists_thenReturnRequest() {
        // given
        User user = createUser(1L);
        ItemRequest request = createRequest(user);

        when(userService.validateUserExists(1L)).thenReturn(true);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(itemService.getItemsByRequestId(1L)).thenReturn(List.of());

        // when
        ItemRequestDto result = requestService.getRequestById(1L, 1L);

        // then
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getRequestById_whenRequestNotExists_thenThrowException() {
        // given
        when(userService.validateUserExists(1L)).thenReturn(true);
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class,
                () -> requestService.getRequestById(999L, 1L));
    }

    @Test
    void getAllRequests_whenOtherUsersExist_thenReturnRequests() {
        // given
        User user2 = createUser(2L);
        ItemRequest request = createRequest(user2);
        Pageable pageable = PageRequest.of(0, 10);

        when(userService.validateUserExists(1L)).thenReturn(true);
        when(requestRepository.findAllByRequesterIdNotOrderByCreatedDesc(1L, pageable))
                .thenReturn(List.of(request));
        when(itemService.getItemsByRequestId(1L)).thenReturn(List.of());

        // when
        List<ItemRequestDto> result = requestService.getAllRequests(1L, pageable);

        // then
        assertEquals(1, result.size());
        assertEquals(2L, result.getFirst().getRequesterId());
    }

    private ItemRequestDto createRequestDto() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need drill");
        return dto;
    }

    private User createUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setName("Test User %d".formatted(id)); // Используем formatted вместо конкатенации
        user.setEmail("test%d@email.com".formatted(id)); // Используем formatted вместо конкатенации
        return user;
    }

    private ItemRequest createRequest(User user) {
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Need drill");
        request.setRequester(user);
        request.setCreated(LocalDateTime.now());
        return request;
    }
}