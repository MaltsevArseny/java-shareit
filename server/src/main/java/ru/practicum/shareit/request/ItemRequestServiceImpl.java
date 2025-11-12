package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;

    @Override
    @Transactional
    public ItemRequest createRequest(ItemRequest request) {
        request.setCreated(LocalDateTime.now());
        return requestRepository.save(request);
    }

    @Override
    public ItemRequest getRequestById(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found with id: " + requestId));
    }

    @Override
    public List<ItemRequest> getUserRequests(Long userId) {
        return requestRepository.findByRequesterIdOrderByCreatedDesc(userId);
    }

    @Override
    public List<ItemRequest> getOtherUsersRequests(Long userId) {
        return requestRepository.findOtherUsersRequests(userId);
    }
}