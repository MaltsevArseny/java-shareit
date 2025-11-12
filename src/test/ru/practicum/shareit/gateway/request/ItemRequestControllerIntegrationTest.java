package ru.practicum.shareit.gateway.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.client.ItemRequestClient;
import ru.practicum.shareit.gateway.request.dto.ItemRequestDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ItemRequestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    void shouldHandleAllEndpoints() throws Exception {
        // Mock responses
        when(itemRequestClient.create(any(ItemRequestDto.class), anyLong()))
                .thenReturn(org.springframework.http.ResponseEntity.ok().build());
        when(itemRequestClient.getByUserId(anyLong()))
                .thenReturn(org.springframework.http.ResponseEntity.ok().build());
        when(itemRequestClient.getAll(anyLong(), anyInt(), anyInt()))
                .thenReturn(org.springframework.http.ResponseEntity.ok().build());
        when(itemRequestClient.getById(anyLong(), anyLong()))
                .thenReturn(org.springframework.http.ResponseEntity.ok().build());

        // Test POST /requests
        ItemRequestDto createDto = new ItemRequestDto();
        createDto.setDescription("Valid description");
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk());

        // Test GET /requests
        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        // Test GET /requests/all
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        // Test GET /requests/{requestId}
        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnErrorResponseWhenClientReturnsError() throws Exception {
        // Given
        when(itemRequestClient.create(any(ItemRequestDto.class), anyLong()))
                .thenReturn(org.springframework.http.ResponseEntity.badRequest().body("Error message"));

        ItemRequestDto createDto = new ItemRequestDto();
        createDto.setDescription("Valid description");

        // When & Then
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }
}