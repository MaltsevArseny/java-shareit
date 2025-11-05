package ru.yandex.practicum.shareit.item;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        if (item == null) {
            return null;
        }
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable()
        );
    }

    public static Item toItem(ItemDto itemDto, Long ownerId) {
        if (itemDto == null) {
            return null;
        }
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                ownerId
        );
    }
}