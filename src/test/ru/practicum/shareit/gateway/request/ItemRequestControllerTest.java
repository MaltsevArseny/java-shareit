package ru.practicum.shareit.gateway.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.client.ItemRequestClient;
import ru.practicum.shareit.gateway.request.dto.ItemRequestDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    void create_shouldReturnBadRequestWhenDescriptionIsBlank() throws Exception {
        // Given
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("");

        // When & Then
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturnBadRequestWhenDescriptionIsNull() throws Exception {
        // Given
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription(null);

        // When & Then
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturnOkWhenValid() throws Exception {
        // Given
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need a drill");

        when(itemRequestClient.create(any(ItemRequestDto.class), eq(1L)))
                .thenReturn(org.springframework.http.ResponseEntity.ok().build());

        // When & Then
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void create_shouldReturnBadRequestWhenMissingUserIdHeader() throws Exception {
        // Given
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need a drill");

        // When & Then
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByUserId_shouldReturnOk() throws Exception {
        // Given
        when(itemRequestClient.getByUserId(1L))
                .thenReturn(org.springframework.http.ResponseEntity.ok().build());

        // When & Then
        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_shouldReturnOk() throws Exception {
        // Given
        when(itemRequestClient.getAll(eq(1L), eq(0), eq(10)))
                .thenReturn(org.springframework.http.ResponseEntity.ok().build());

        // When & Then
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_shouldUseDefaultPagination() throws Exception {
        // Given
        when(itemRequestClient.getAll(eq(1L), eq(0), eq(10)))
                .thenReturn(org.springframework.http.ResponseEntity.ok().build());

        // When & Then
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_shouldReturnBadRequestWhenFromIsNegative() throws Exception {
        // When & Then
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_shouldReturnBadRequestWhenSizeIsZero() throws Exception {
        // When & Then
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_shouldReturnOk() throws Exception {
        // Given
        Long requestId = 1L;
        when(itemRequestClient.getById(requestId, 1L))
                .thenReturn(org.springframework.http.ResponseEntity.ok().build());

        // When & Then
        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getById_shouldReturnBadRequestWhenMissingUserIdHeader() throws Exception {
        // Given
        Long requestId = 1L;

        // When & Then
        mockMvc.perform(get("/requests/{requestId}", requestId))
                .andExpect(status().isBadRequest());
    }
}