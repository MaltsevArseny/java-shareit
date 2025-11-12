package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository requestRepository;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    @Test
    void createRequest_ShouldSetCreatedDateAndSave() {
        ItemRequest request = new ItemRequest(null, "Need item", null, null);

        when(requestRepository.save(any(ItemRequest.class))).thenAnswer(invocation -> {
            ItemRequest saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        ItemRequest result = requestService.createRequest(request);

        assertNotNull(result);
        assertNotNull(result.getCreated());
        verify(requestRepository, times(1)).save(request);
    }

    @Test
    void getRequestById_WhenRequestExists_ShouldReturnRequest() {
        Long requestId = 1L;
        ItemRequest request = new ItemRequest(requestId, "Need item", null, LocalDateTime.now());

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

        ItemRequest result = requestService.getRequestById(requestId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        verify(requestRepository, times(1)).findById(requestId);
    }

    @Test
    void getUserRequests_ShouldReturnUserRequests() {
        Long userId = 1L;
        List<ItemRequest> requests = List.of(
                new ItemRequest(1L, "Request 1", null, LocalDateTime.now()),
                new ItemRequest(2L, "Request 2", null, LocalDateTime.now().minusDays(1))
        );

        when(requestRepository.findByRequesterIdOrderByCreatedDesc(userId)).thenReturn(requests);

        List<ItemRequest> result = requestService.getUserRequests(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(requestRepository, times(1)).findByRequesterIdOrderByCreatedDesc(userId);
    }

    @Test
    void getOtherUsersRequests_ShouldReturnOtherUsersRequests() {
        Long userId = 1L;
        List<ItemRequest> requests = List.of(
                new ItemRequest(2L, "Other Request", null, LocalDateTime.now())
        );

        when(requestRepository.findOtherUsersRequests(userId)).thenReturn(requests);

        List<ItemRequest> result = requestService.getOtherUsersRequests(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(requestRepository, times(1)).findOtherUsersRequests(userId);
    }
}