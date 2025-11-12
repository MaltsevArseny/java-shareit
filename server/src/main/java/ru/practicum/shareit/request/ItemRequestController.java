package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService requestService;
    private final ItemService itemService;

    @PostMapping
    public ItemRequest createRequest(@RequestBody ItemRequest request) {
        return requestService.createRequest(request);
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponse getRequestById(@PathVariable Long requestId) {
        ItemRequest request = requestService.getRequestById(requestId);
        List<Item> items = itemService.getItemsByRequestId(requestId);
        return new ItemRequestResponse(request, items);
    }

    @GetMapping
    public List<ItemRequestResponse> getUserRequests(@RequestParam Long userId) {
        List<ItemRequest> requests = requestService.getUserRequests(userId);
        return requests.stream()
                .map(request -> {
                    List<Item> items = itemService.getItemsByRequestId(request.getId());
                    return new ItemRequestResponse(request, items);
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/all")
    public List<ItemRequestResponse> getOtherUsersRequests(@RequestParam Long userId) {
        List<ItemRequest> requests = requestService.getOtherUsersRequests(userId);
        return requests.stream()
                .map(request -> {
                    List<Item> items = itemService.getItemsByRequestId(request.getId());
                    return new ItemRequestResponse(request, items);
                })
                .collect(Collectors.toList());
    }

    // DTO для ответа с items
    @Getter
    @AllArgsConstructor
    public static class ItemRequestResponse {
        private Long id;
        private String description;
        private Long requesterId;
        private java.time.LocalDateTime created;
        private List<ItemResponse> items;

        public ItemRequestResponse(ItemRequest request, List<Item> items) {
            this.id = request.getId();
            this.description = request.getDescription();
            this.requesterId = request.getRequester().getId();
            this.created = request.getCreated();
            this.items = items.stream()
                    .map(ItemResponse::new)
                    .collect(Collectors.toList());
        }
    }

    @Getter
    @AllArgsConstructor
    public static class ItemResponse {
        private Long id;
        private String name;
        private String description;
        private Boolean available;
        private Long ownerId;
        private Long requestId;

        public ItemResponse(Item item) {
            this.id = item.getId();
            this.name = item.getName();
            this.description = item.getDescription();
            this.available = item.getAvailable();
            this.ownerId = item.getOwner().getId();
            this.requestId = item.getRequest() != null ? item.getRequest().getId() : null;
        }
    }
}