package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.client.ItemRequestClient;
import ru.practicum.shareit.dto.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequestController;

import static org.mockito.ArgumentMatchers.*;
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
    private ItemRequestClient client;

    ItemRequestControllerTest(ItemRequestClient client) {
        this.client = client;
    }

    @Test
    void createRequest_whenValid_thenReturnOk() throws Exception {
        ItemRequestDto requestDto = createValidRequestDto();
        ItemRequestDto responseDto = createResponseRequestDto();

        when(client.createRequest(any(), eq(1L)))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need drill"));
    }

    @Test
    void createRequest_whenNoUserId_thenReturnBadRequest() throws Exception {
        ItemRequestDto requestDto = createValidRequestDto();

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRequest_whenInvalidUserId_thenReturnBadRequest() throws Exception {
        ItemRequestDto requestDto = createValidRequestDto();

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 0L) // invalid user ID
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserRequests_whenValid_thenReturnOk() throws Exception {
        when(client.getUserRequests(1L))
                .thenReturn(ResponseEntity.ok("[]"));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRequests_whenValid_thenReturnOk() throws Exception {
        when(client.getAllRequests(eq(1L), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok("[]"));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getRequestById_whenValid_thenReturnOk() throws Exception {
        ItemRequestDto responseDto = createResponseRequestDto();

        when(client.getRequestById(1L, 1L))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    private ItemRequestDto createValidRequestDto() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need drill");
        return dto;
    }

    private ItemRequestDto createResponseRequestDto() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(1L);
        dto.setDescription("Need drill");
        // created field might not be needed for gateway DTO
        return dto;
    }
}