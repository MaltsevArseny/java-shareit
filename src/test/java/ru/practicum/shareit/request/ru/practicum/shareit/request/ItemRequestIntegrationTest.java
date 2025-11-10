package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@SuppressWarnings("unused")
class ItemRequestIntegrationTest {
    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private itemrequestrepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void createRequestAndAddItem_whenValid_thenSuccess() {
        // given
        User requester = userRepository.save(createUser("requester@email.com"));
        User owner = userRepository.save(createUser("owner@email.com"));

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need a powerful drill");

        // when - create request
        ItemRequestDto createdRequest = requestService.createRequest(requestDto, requester.getId());

        // when - add item for this request
        Item item = new Item();
        item.setName("Bosch Drill");
        item.setDescription("Powerful drill for construction");
        item.setAvailable(true);
        item.setOwner(owner);

        // Получаем ItemRequest из репозитория
        ItemRequest itemRequest = requestRepository.findById(createdRequest.getId())
                .orElseThrow(() -> new RuntimeException("Request not found"));
        item.setRequest(itemRequest);

        itemRepository.save(item);

        // then
        ItemRequestDto foundRequest = requestService.getRequestById(createdRequest.getId(), requester.getId());
        assertThat(foundRequest.getItems()).hasSize(1);
        assertThat(foundRequest.getItems().getFirst().getName()).isEqualTo("Bosch Drill");
    }

    private User createUser(String email) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        return user;
    }
}