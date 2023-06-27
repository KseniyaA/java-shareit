package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.ItemDtoWithBookingDateResponse;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemService itemService;

    @Autowired
    private MockMvc mvc;

    private Item makeItem() {
        return Item.builder()
                .id(1)
                .name("name")
                .description("desc")
                .available(false)
                .owner(User.builder().id(1L).build())
                .build();
    }

    @Test
    void saveNewItemSuccessTest() throws Exception {
        Item itemDtoIn = makeItem();

        when(itemService.add(Mockito.any(), anyLong())).thenReturn(itemDtoIn);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDtoIn))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoIn.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoIn.getName())))
                .andExpect(jsonPath("$.description", is(itemDtoIn.getDescription())))
                .andExpect(jsonPath("$.owner", notNullValue()))
                .andExpect(jsonPath("$.available", is(itemDtoIn.getAvailable())))
                .andExpect(jsonPath("$.requestId", nullValue()))
                .andExpect(jsonPath("$.comments").doesNotExist())
                .andReturn();
    }

    @Test
    void saveNewItemNoUserFailTest() throws Exception {
        Item itemDtoIn = makeItem();

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void saveNewItemEmptyFieldsTest() throws Exception {
        Item itemDtoIn = makeItem();
        itemDtoIn.setName(null);
        itemDtoIn.setDescription(null);
        itemDtoIn.setAvailable(null);

        MvcResult mvcResult = mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(itemDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Violations violations = mapper.readValue(contentAsString, Violations.class);
        List<String> errs = violations.getViolations().stream().map(x -> x.message).collect(Collectors.toList());
        assertThat(errs.contains("Поле name не должно быть пустым"), is(true));
        assertThat(errs.contains("Поле description не должно быть пустым"), is(true));
        assertThat(errs.contains("Поле available не должно быть пустым"), is(true));
    }

    @Test
    void updateItemNoUserFailTest() throws Exception {
        Item itemDtoIn = makeItem();

        mvc.perform(patch("/items/{itemId}", 1)
                        .content(mapper.writeValueAsString(itemDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void updateItemMaxSizeFailTest() throws Exception {
        Item itemDtoIn = makeItem();
        itemDtoIn.setName("a".repeat(256));

        MvcResult mvcResult = mvc.perform(patch("/items/{itemId}", 1)
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(itemDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Violations violations = mapper.readValue(contentAsString, Violations.class);
        List<String> errs = violations.getViolations().stream().map(x -> x.message).collect(Collectors.toList());
        assertThat(errs.contains("size must be between 0 and 255"), is(true));
    }

    @Test
    void getItemSuccessTest() throws Exception {
        Item itemDtoIn = makeItem();

        when(itemService.get(anyLong(), anyLong())).thenReturn(itemDtoIn);

        mvc.perform(get("/items/{itemId}", 999)
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(itemDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoIn.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoIn.getName())))
                .andExpect(jsonPath("$.description", is(itemDtoIn.getDescription())))
                .andExpect(jsonPath("$.owner", notNullValue()))
                .andExpect(jsonPath("$.available", is(itemDtoIn.getAvailable())))
                .andExpect(jsonPath("$.request", nullValue()))
                .andExpect(jsonPath("$.lastBooking", nullValue()))
                .andExpect(jsonPath("$.nextBooking", nullValue()))
                .andExpect(jsonPath("$.comments", nullValue()));
    }

    @Test
    void getNoItemFailTest() throws Exception {
        Item itemDtoIn = makeItem();

        Mockito.doThrow(EntityNotFoundException.class).when(itemService).get(anyLong(), anyLong());

        mvc.perform(get("/items/{itemId}", 999)
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(itemDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException() instanceof EntityNotFoundException,
                        is(true)));
    }

    @Test
    void getItemNoUserFailTest() throws Exception {
        Item itemDtoIn = makeItem();

        mvc.perform(get("/items/{itemId}", 1)
                        .content(mapper.writeValueAsString(itemDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getAllByUserNoUserTest() throws Exception {
        Item itemDtoIn = makeItem();

        mvc.perform(get("/items")
                        .content(mapper.writeValueAsString(itemDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getAllByUserTest() throws Exception {
        Item itemDtoIn = makeItem();

        when(itemService.getAllByUser(anyLong(), anyInt(), anyInt())).thenReturn(Arrays.asList(itemDtoIn));

        MvcResult mvcResult = mvc.perform(get("/items")
                        .param("from", String.valueOf(1))
                        .param("size", String.valueOf(1))
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(itemDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<ItemDtoWithBookingDateResponse> users = mapper.readValue(contentAsString, List.class);
        assertThat(users.size(), equalTo(1));
    }

    @Test
    void searchSuccessTest() throws Exception {
        Item itemDtoIn = makeItem();

        when(itemService.searchByText(anyString(), anyInt(), anyInt())).thenReturn(Arrays.asList(itemDtoIn));

        MvcResult mvcResult = mvc.perform(get("/items/search")
                        .param("text", String.valueOf("text"))
                        .param("from", String.valueOf(1))
                        .param("size", String.valueOf(1))
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(itemDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<ItemDtoWithBookingDateResponse> users = mapper.readValue(contentAsString, List.class);
        assertThat(users.size(), equalTo(1));
    }

    @Test
    void searchNoTextParamTest() throws Exception {
        Item itemDtoIn = makeItem();

        when(itemService.searchByText(anyString(), anyInt(), anyInt())).thenReturn(Arrays.asList(itemDtoIn));

        MvcResult mvcResult = mvc.perform(get("/items/search")
                        .param("from", String.valueOf(1))
                        .param("size", String.valueOf(1))
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(itemDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(contentAsString.contains("Required request parameter 'text' for method parameter type String is not present"));
    }

    @Test
    void saveCommentSuccessTest() throws Exception {
        Item itemDtoIn = makeItem();
        Comment commentDtoIn = Comment.builder().text("text").build();
        Comment commentDtoOut = Comment.builder()
                .id(1L)
                .text("text")
                .author(User.builder().id(1L).name("authorName").build())
                .item(itemDtoIn)
                .created(LocalDateTime.now())
                .build();

        when(itemService.createComment(any(), anyLong(), anyLong())).thenReturn(commentDtoOut);

        mvc.perform(post("/items/{itemId}/comment", 1)
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(commentDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDtoOut.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDtoOut.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDtoOut.getAuthor().getName())))
                .andExpect(jsonPath("$.created", notNullValue()))
                .andReturn();
    }

    @Test
    void saveCommentWithNoUserTest() throws Exception {
        Item itemDtoIn = makeItem();
        Comment commentDtoIn = Comment.builder().text("text").build();
        Comment commentDtoOut = Comment.builder()
                .id(1L)
                .text("text")
                .author(User.builder().id(1L).name("authorName").build())
                .item(itemDtoIn)
                .created(LocalDateTime.now())
                .build();

        when(itemService.createComment(any(), anyLong(), anyLong())).thenReturn(commentDtoOut);

        mvc.perform(post("/items/{itemId}/comment", 1)
                        .content(mapper.writeValueAsString(commentDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void saveCommentWithNoTextTest() throws Exception {
        Comment commentDtoIn = Comment.builder().text(null).build();

        MvcResult mvcResult = mvc.perform(post("/items/{itemId}/comment", 1)
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(commentDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Violations violations = mapper.readValue(contentAsString, Violations.class);
        List<String> errs = violations.getViolations().stream().map(x -> x.message).collect(Collectors.toList());
        assertThat(errs.contains("Поле text не должно быть пустым"), is(true));
    }

    @Test
    void saveCommentWithMaxSizeTest() throws Exception {
        Comment commentDtoIn = Comment.builder().text("a".repeat(301)).build();

        MvcResult mvcResult = mvc.perform(post("/items/{itemId}/comment", 1)
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(commentDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Violations violations = mapper.readValue(contentAsString, Violations.class);
        List<String> errs = violations.getViolations().stream().map(x -> x.message).collect(Collectors.toList());
        assertThat(errs.contains("size must be between 0 and 300"), is(true));
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
