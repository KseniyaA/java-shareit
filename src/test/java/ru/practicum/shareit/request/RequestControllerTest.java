package ru.practicum.shareit.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.common.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RequestController.class)
public class RequestControllerTest {
    @Autowired
    ObjectMapper mapper;

    @MockBean
    RequestService requestService;

    @Autowired
    private MockMvc mvc;

    private User makeUser(Long id, String name, String email) {
        return User.builder().id(id).name(name).email(email).build();
    }

    private Item makeItem(Long id, String name, String desc, User owner, Boolean available) {
        return Item.builder().id(id).name(name).description(desc).owner(owner).available(available).build();
    }

    private Request makeRequest(String desc) {
        return Request.builder()
                .description(desc)
                .build();
    }

    @Test
    void saveNewRequestSuccessTest() throws Exception {
        Request requestDto = makeRequest("desc req");
        User itemOwner = makeUser(2L, "nameUser", "email");
        makeItem(2L, "name", "desc", itemOwner, true);
        Request reqResp = Request.builder().id(1L).description("desc req").created(LocalDateTime.now()).build();

        when(requestService.add(any(), anyLong())).thenReturn(reqResp);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description", is("desc req")))
                .andExpect(jsonPath("$.created", notNullValue()))
                .andExpect(jsonPath("$.items").exists())
                .andReturn();
    }

    @Test
    void saveNewRequestNoUserTest() throws Exception {
        Request requestDto = makeRequest("desc req");
        Request reqResp = Request.builder().id(1L).description("desc req").created(LocalDateTime.now()).build();

        when(requestService.add(any(), anyLong())).thenReturn(reqResp);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andReturn();
    }

    @Test
    void saveWithEmptyDescTest() throws Exception {
        Request requestDto = makeRequest(null);

        MvcResult mvcResult = mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Violations violations = mapper.readValue(contentAsString, Violations.class);
        List<String> errs = violations.getViolations().stream().map(x -> x.message).collect(Collectors.toList());
        assertThat(errs.contains("Поле description не должно быть пустым"), is(true));
    }

    @Test
    void getRequestsByUserSuccessTest() throws Exception {
        Request requestDto = makeRequest("desc req");
        User itemOwner = makeUser(2L, "nameUser", "email");
        makeItem(2L, "name", "desc", itemOwner, true);
        Request reqResp = Request.builder().id(1L).description("desc req").created(LocalDateTime.now()).build();

        when(requestService.getAllByUser(anyLong())).thenReturn(List.of(reqResp));

        mvc.perform(get("/requests")
                        .content(mapper.writeValueAsString(requestDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getRequestsByUserNoUserTest() throws Exception {
        Request requestDto = makeRequest("desc req");

        mvc.perform(get("/requests")
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getAllRequestsTest() throws Exception {
        Request requestDto = makeRequest("desc req");

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", String.valueOf(0))
                        .param("size", String.valueOf(10))
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getAllValidationExceptionTest() throws Exception {
        Request requestDto = makeRequest("desc req");
        when(requestService.getAll(anyLong(), anyInt(), anyInt())).thenThrow(
                new ValidationException("Некорректные значения параметров"));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", String.valueOf(0))
                        .param("size", String.valueOf(10))
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestsByIdTest() throws Exception {
        Request requestDto = makeRequest("desc req");

        Request reqResp = Request.builder()
                .id(1L)
                .description("desc req")
                .created(LocalDateTime.now())
                .build();

        when(requestService.getById(anyLong(), anyLong())).thenReturn(reqResp);

        mvc.perform(get("/requests/{requestId}", 1L)
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description", is("desc req")))
                .andExpect(jsonPath("$.created", notNullValue()))
                .andExpect(jsonPath("$.items").exists())
                .andReturn();
    }

    @Test
    void getRequestsByIdNotFoundExTest() throws Exception {
        Request requestDto = makeRequest("desc req");

        Request reqResp = Request.builder().id(1L).description("desc req").created(LocalDateTime.now()).build();

        when(requestService.getById(anyLong(), anyLong())).thenThrow(new EntityNotFoundException("Ошибка поиска по id"));

        mvc.perform(get("/requests/{requestId}", 1L)
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @NoArgsConstructor
    @Getter
    private static class Violations {
        @JsonProperty("violations")
        public List<Violation> violations;
    }

    @NoArgsConstructor
    private static class Violation {
        @JsonProperty("fieldName") public String fieldName;
        @JsonProperty("message") public String message;
    }
}
