package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RequestServiceDbTest {
    private final EntityManager em;
    private final RequestService requestService;
    private final UserService userService;
    private final ItemService itemService;

    private User makeUser(String name, String email) {
        return User.builder().name(name).email(email).build();
    }

    private Item makeItem(Long id, String name, String desc, User owner, Boolean available) {
        return Item.builder().id(id).name(name).description(desc).owner(owner).available(available).build();
    }

    private Request makeRequest(String desc) {
        return Request.builder()
                .description(desc)
                .build();
    }

    @BeforeEach
    private void clear() {
        em.createQuery("delete from Item").executeUpdate();
        em.createQuery("delete from User").executeUpdate();
        em.createQuery("delete from Request").executeUpdate();
    }

    @Test
    void addRequestSuccessTest() {
        User createdUser = userService.create(makeUser("userName", "userEmail@ya.ru"));
        Request requestDto = makeRequest("desc");

        Request createdRequest = requestService.add(requestDto, createdUser.getId());

        TypedQuery<Request> query = em.createQuery("Select r from Request r where r.description = :desc", Request.class);
        Request request = query
                .setParameter("desc", requestDto.getDescription())
                .getSingleResult();

        assertThat(request.getId(), notNullValue());
        assertThat(createdRequest.getId(), equalTo(request.getId()));
        assertThat(createdRequest.getRequester().getId(), equalTo(request.getRequester().getId()));
        assertThat(createdRequest.getCreated(), equalTo(request.getCreated()));
        assertThat(createdRequest.getItems().size(), equalTo(request.getItems().size()));
    }

    @Test
    void getAllByUserSuccessTest() {
        User createdUser = userService.create(makeUser("userName", "userEmail@ya.ru"));
        Request requestDto1 = makeRequest("desc1");
        Request requestDto2 = makeRequest("desc2");
        User createdUser2 = userService.create(makeUser("userName2", "userEmail2@ya.ru"));
        Request requestDto3 = makeRequest("desc1");

        Request createdRequest1 = requestService.add(requestDto1, createdUser.getId());
        Request createdRequest2 = requestService.add(requestDto2, createdUser.getId());
        Request createdRequest3 = requestService.add(requestDto3, createdUser2.getId());

        List<Request> allByUser = requestService.getAllByUser(createdUser.getId());

        TypedQuery<Request> query = em.createQuery("Select r from Request r join r.requester as u where u.id = :id", Request.class);
        List<Request> requestDb = query
                .setParameter("id", createdUser.getId())
                .getResultList();

        assertThat(requestDb.size(), equalTo(allByUser.size()));
        assertThat(requestDb.size(), equalTo(2));
    }

    @Test
    void getAllSuccessTest() {
        User createdUser = userService.create(makeUser("userName", "userEmail@ya.ru"));
        Request requestDto1 = makeRequest("desc1");
        Request requestDto2 = makeRequest("desc2");
        User createdUser2 = userService.create(makeUser("userName2", "userEmail2@ya.ru"));
        Request requestDto3 = makeRequest("desc1");

        Request createdRequest1 = requestService.add(requestDto1, createdUser.getId());
        Request createdRequest2 = requestService.add(requestDto2, createdUser.getId());
        Request createdRequest3 = requestService.add(requestDto3, createdUser2.getId());

        List<Request> allByUser = requestService.getAll(createdUser.getId(), 0, 10);

        assertThat(allByUser.size(), equalTo(0));

        Item createdItemByRequest = itemService.add(Item.builder().name("name").description("desc").available(true).request(createdRequest1).build()
                , createdUser2.getId());
        allByUser = requestService.getAll(createdUser2.getId(), 0, 10);
        assertThat(allByUser.size(), equalTo(1));
    }

    @Test
    void getByIdSuccessTest() {
        User createdUser = userService.create(makeUser("userName", "userEmail@ya.ru"));
        Request requestDto1 = makeRequest("desc1");
        Request requestDto2 = makeRequest("desc2");

        Request createdRequest1 = requestService.add(requestDto1, createdUser.getId());
        Request createdRequest2 = requestService.add(requestDto2, createdUser.getId());

        Request byId = requestService.getById(createdRequest1.getId(), createdUser.getId());

        TypedQuery<Request> query = em.createQuery("Select r from Request r where r.id = :id", Request.class);
        List<Request> requestDb = query
                .setParameter("id", createdRequest1.getId())
                .getResultList();

        assertThat(requestDb.size(), equalTo(1));
        assertThat(requestDb.get(0).getDescription(), equalTo("desc1"));

    }
}
