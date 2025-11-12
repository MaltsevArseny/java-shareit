package ru.practicum.shareit.request;

import java.util.List;

public interface ItemRequestService {
    ItemRequest createRequest(ItemRequest request);

    ItemRequest getRequestById(Long requestId);

    List<ItemRequest> getUserRequests(Long userId);

    List<ItemRequest> getOtherUsersRequests(Long userId);
}