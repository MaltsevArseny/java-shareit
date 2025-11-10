package ru.practicum.shareit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@SuppressWarnings("unused")
class ItemRequestDtoJsonTest {
    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void serialize_whenValid_thenSuccess() throws IOException {
        // given
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(1L);
        dto.setDescription("Need a drill");

        // when
        JsonContent<ItemRequestDto> result = json.write(dto);

        // then
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Need a drill");
        assertThat(result).extractingJsonPathNumberValue("$.id")
                .isEqualTo(1);
    }

    @Test
    void deserialize_whenValidJson_thenSuccess() throws IOException {
        // given
        String content = "{\"description\":\"Need a drill\"}";

        // when
        ItemRequestDto dto = json.parse(content).getObject();

        // then
        assertThat(dto.getDescription()).isEqualTo("Need a drill");
        assertThat(dto.getId()).isNull();
    }

    @Test
    void deserialize_whenEmptyDescription_thenSuccess() throws IOException {
        // given
        String content = "{\"description\":\"\"}";

        // when
        ItemRequestDto dto = json.parse(content).getObject();

        // then
        assertThat(dto.getDescription()).isEmpty();
    }

    @Test
    void deserialize_whenFullJson_thenSuccess() throws IOException {
        // given
        String content = "{\"id\": 2, \"description\":\"Need hammer\"}";

        // when
        ItemRequestDto dto = json.parse(content).getObject();

        // then
        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getDescription()).isEqualTo("Need hammer");
    }
}