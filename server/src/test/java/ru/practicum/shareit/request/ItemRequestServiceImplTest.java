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
class ItemRequestServiceImplTest {
    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void getByIdShouldReturnRequestWithResponseItems() {
        User requestor = userRepository.save(user("requestor", "requestor@mail.com"));
        User owner = userRepository.save(user("owner", "owner@mail.com"));

        ItemRequestDto createdRequest = requestService.create(
                requestor.getId(),
                new ItemRequestDto(null, "Need ladder", null, null)
        );

        Item item = new Item();
        item.setName("Ladder");
        item.setDescription("Aluminium ladder");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(new ItemRequest(createdRequest.getId(), createdRequest.getDescription(), requestor,
                createdRequest.getCreated()));
        itemRepository.save(item);

        ItemRequestDto result = requestService.getById(owner.getId(), createdRequest.getId());

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().getFirst().getName()).isEqualTo("Ladder");
        assertThat(result.getItems().getFirst().getOwnerId()).isEqualTo(owner.getId());
    }

    private User user(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}
