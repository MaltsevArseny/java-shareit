package ru.practicum.shareit.request;

import ru.practicum.shareit.user.User;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User requester) {
        if (itemRequestDto == null || requester == null) {
            return null;
        }

        ItemRequest request = new ItemRequest();
        request.setDescription(itemRequestDto.getDescription());
        request.setRequester(requester);
        return request;
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        if (itemRequest == null) {
            return null;
        }

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(itemRequest.getId());
        dto.setDescription(itemRequest.getDescription());
        dto.setCreated(itemRequest.getCreated());

        if (itemRequest.getRequester() != null) {
            dto.setRequesterId(itemRequest.getRequester().getId());
        }

        return dto;
    }
}