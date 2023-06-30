package ru.practicum.shareit.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDtoResponse;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {
    @Autowired
    ObjectMapper mapper;

    @MockBean
    UserService userService;

    @Autowired
    private MockMvc mvc;

    private User makeUser(Long id, String name, String email) {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .build();
    }

    private User makeUser(String name, String email) {
        return User.builder()
                .name(name)
                .email(email)
                .build();
    }

    @Test
    void saveNewUserSuccessTest() throws Exception {
        User userDtoIn = makeUser("name", "name@ya.ru");
        User userDtoOut = makeUser(1L, "name", "name@ya.ru");

        when(userService.create(any()))
                .thenReturn(userDtoOut);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDtoOut.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDtoOut.getName())))
                .andExpect(jsonPath("$.email", is(userDtoOut.getEmail())));
    }

    @Test
    void saveNoNameUserFailTest() throws Exception {
        User userDtoIn = makeUser(null, "name@ya.ru");

        MvcResult mvcResult = mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Violations violations = mapper.readValue(contentAsString, Violations.class);
        List<String> errs = violations.getViolations().stream().map(x -> x.message).collect(Collectors.toList());
        assertThat(errs.contains("Поле name не должно быть пустым"), is(true));
    }

    @Test
    void saveNoEmailUserFailTest() throws Exception {
        User userDtoIn = makeUser("name", null);

        MvcResult mvcResult = mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Violations violations = mapper.readValue(contentAsString, Violations.class);
        List<String> errs = violations.getViolations().stream().map(x -> x.message).collect(Collectors.toList());
        assertThat(errs.contains("Поле email не должно быть пустым"), is(true));
    }

    @Test
    void saveInvalidEmailUserFailTest() throws Exception {
        User userDtoIn = makeUser("name", "ya.ru");

        MvcResult mvcResult = mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Violations violations = mapper.readValue(contentAsString, Violations.class);
        List<String> errs = violations.getViolations().stream().map(x -> x.message).collect(Collectors.toList());
        assertThat(errs.contains("Не верный формат электронной почты"), is(true));
    }

    @Test
    void updateUserSuccessTest() throws Exception {
        User userDtoOut = makeUser(1L, "name", "name@ya.ru");

        when(userService.update(any())).thenReturn(userDtoOut);

        mvc.perform(patch("/users/{userId}", 1L)
                        .content(mapper.writeValueAsString(userDtoOut))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDtoOut.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDtoOut.getName())))
                .andExpect(jsonPath("$.email", is(userDtoOut.getEmail())));
    }

    @Test
    void updateUserInvalidEmailFailTest() throws Exception {
        User userDtoOut = makeUser("name", "name.ru");

        MvcResult mvcResult = mvc.perform(patch("/users/{userId}", 1L)
                        .content(mapper.writeValueAsString(userDtoOut))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Violations violations = mapper.readValue(contentAsString, Violations.class);
        List<String> errs = violations.getViolations().stream().map(x -> x.message).collect(Collectors.toList());
        assertThat(errs.contains("Не верный формат электронной почты"), is(true));
    }

    @Test
    void deleteByIdSuccessTest() throws Exception {
        Mockito.doNothing().when(userService).deleteById(Mockito.anyLong());

        mvc.perform(delete("/users/{userId}", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void deleteByIdNotFoundTest() throws Exception {
        Mockito.doThrow(EntityNotFoundException.class).when(userService).deleteById(Mockito.anyLong());

        mvc.perform(delete("/users/{userId}", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException() instanceof EntityNotFoundException,
                        is(true)));
    }

    @Test
    void getByIdSuccessTest() throws Exception {
        User userDtoOut = makeUser(1L, "name", "name@ya.ru");

        when(userService.getById(any())).thenReturn(userDtoOut);

        mvc.perform(get("/users/{id}", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDtoOut.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDtoOut.getName())))
                .andExpect(jsonPath("$.email", is(userDtoOut.getEmail())));
    }

    @Test
    void getByNotExistIdTest() throws Exception {
        Mockito.doThrow(EntityNotFoundException.class).when(userService).getById(Mockito.anyLong());

        mvc.perform(get("/users/{id}", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException() instanceof EntityNotFoundException,
                        is(true)));
    }

    @Test
    void getAllSuccessTest() throws Exception {
        User userDtoOut1 = makeUser(1L, "name", "name@ya.ru");
        User userDtoOut2 = makeUser(2L, "name2", "name2@ya.ru");

        when(userService.getAll()).thenReturn(Arrays.asList(userDtoOut1, userDtoOut2));

        MvcResult mvcResult = mvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<UserDtoResponse> users = mapper.readValue(contentAsString, List.class);
        assertThat(users.size() == 2, is(true));
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
