package ru.practicum.shareit.gateway.request.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeToJson() throws Exception {
        // Given
        ItemRequestDto.ItemDto itemDto = new ItemRequestDto.ItemDto(1L, "Item", 2L, "Description", true, 1L);
        ItemRequestDto requestDto = new ItemRequestDto(1L, "Need item", LocalDateTime.now(), List.of(itemDto));

        // When
        String json = objectMapper.writeValueAsString(requestDto);

        // Then
        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"description\":\"Need item\"");
        assertThat(json).contains("\"items\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        // Given
        String json = "{\"description\":\"Need item\"}";

        // When
        ItemRequestDto requestDto = objectMapper.readValue(json, ItemRequestDto.class);

        // Then
        assertThat(requestDto.getDescription()).isEqualTo("Need item");
    }

    @Test
    void shouldHandleNullItems() throws Exception {
        // Given
        String json = "{\"id\":1,\"description\":\"Test\",\"created\":\"2023-01-01T12:00:00\",\"items\":null}";

        // When
        ItemRequestDto requestDto = objectMapper.readValue(json, ItemRequestDto.class);

        // Then
        assertThat(requestDto.getId()).isEqualTo(1L);
        assertThat(requestDto.getDescription()).isEqualTo("Test");
        assertThat(requestDto.getItems()).isNull();
    }
}