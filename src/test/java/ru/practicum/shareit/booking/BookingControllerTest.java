package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemService itemService;

    @MockBean
    UserService userService;

    @MockBean
    BookingService bookingService;

    @Autowired
    private MockMvc mvc;

    private User makeUser(Long id, String name, String email) {
        return User.builder().id(id).name(name).email(email).build();
    }

    private Item makeItem(Long id, String name, String desc, User owner, Boolean available) {
        return Item.builder().id(id).name(name).description(desc).owner(owner).available(available).build();
    }

    private BookingDtoRequest makeBooking(Long itemId) {
        return BookingDtoRequest.builder()
                .start(LocalDateTime.now().plusMinutes(10))
                .end(LocalDateTime.now().plusDays(1))
                .itemId(itemId)
                .build();
    }

    private Booking makeBookingResult(Long id, Item item, User booker, BookingStatus status, LocalDateTime start, LocalDateTime end) {
        return Booking.builder()
                .id(id)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(status)
                .build();
    }

    @Test
    void addNewBookingSuccessTest() throws Exception {
        User itemOwner = makeUser(1L, "name", "email");
        Item item = makeItem(2L, "name", "desc", itemOwner, true);
        User booker = makeUser(3L, "booker", "bookerEmail");
        BookingDtoRequest bookingDtoIn = makeBooking(2L);
        Booking bookingResult = makeBookingResult(4L, item, booker, BookingStatus.WAITING, bookingDtoIn.getStart(), bookingDtoIn.getEnd());

        when(userService.getById(anyLong())).thenReturn(booker);
        when(bookingService.create(any(), any())).thenReturn(bookingResult);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoIn))
                        .header("X-Sharer-User-Id", booker.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResult.getId()), Long.class))
                .andExpect(jsonPath("$.start", notNullValue()))
                .andExpect(jsonPath("$.end", notNullValue()))
                .andExpect(jsonPath("$.item", notNullValue()))
                .andExpect(jsonPath("$.booker", notNullValue()))
                .andExpect(jsonPath("$.status", is(BookingStatus.WAITING.name())))
                .andReturn();
    }

    @Test
    void addNewBookingNoUserTest() throws Exception {
        BookingDtoRequest bookingDtoIn = makeBooking(2L);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void addNewBookingFailStartEndParamsTest() throws Exception {
        BookingDtoRequest bookingDtoIn = BookingDtoRequest.builder().start(null).end(null).itemId(1L).build();

        MvcResult mvcResult = mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoIn))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Violations violations = mapper.readValue(contentAsString, Violations.class);
        List<String> errs = violations.getViolations().stream().map(x -> x.message).collect(Collectors.toList());
        assertThat(errs.contains("Start must be before end or not null"), is(true));
    }

    @Test
    void addNewBookingFailStartAfterEndParamsTest() throws Exception {
        BookingDtoRequest bookingDtoIn = BookingDtoRequest.builder().start(LocalDateTime.now().plusMinutes(10))
                .end(LocalDateTime.now().plusMinutes(1)).itemId(1L).build();

        MvcResult mvcResult = mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoIn))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Violations violations = mapper.readValue(contentAsString, Violations.class);
        List<String> errs = violations.getViolations().stream().map(x -> x.message).collect(Collectors.toList());
        assertThat(errs.contains("Start must be before end or not null"), is(true));
    }

    @Test
    void addNewBookingEmptyItemIdParamsTest() throws Exception {
        BookingDtoRequest bookingDtoIn = BookingDtoRequest.builder().start(LocalDateTime.now().plusMinutes(1))
                .end(LocalDateTime.now().plusMinutes(10)).itemId(null).build();

        MvcResult mvcResult = mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoIn))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Violations violations = mapper.readValue(contentAsString, Violations.class);
        List<String> errs = violations.getViolations().stream().map(x -> x.fieldName + " " + x.message).collect(Collectors.toList());
        assertThat(errs.contains("itemId must not be null"), is(true));
    }

    @Test
    void approveBookingSuccessTest() throws Exception {
        User itemOwner = makeUser(1L, "name", "email");
        Item item = makeItem(2L, "name", "desc", itemOwner, true);
        User booker = makeUser(3L, "booker", "bookerEmail");
        BookingDtoRequest bookingDtoIn = makeBooking(2L);
        Booking bookingResult = makeBookingResult(4L, item, booker, BookingStatus.WAITING, bookingDtoIn.getStart(), bookingDtoIn.getEnd());

        when(bookingService.approve(anyLong(), anyBoolean(), anyLong())).thenReturn(bookingResult);

        mvc.perform(patch("/bookings/{bookingId}", 1L)
                        .param("approved", String.valueOf(true))
                        .header("X-Sharer-User-Id", booker.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResult.getId()), Long.class))
                .andExpect(jsonPath("$.start", notNullValue()))
                .andExpect(jsonPath("$.end", notNullValue()))
                .andExpect(jsonPath("$.item", notNullValue()))
                .andExpect(jsonPath("$.booker", notNullValue()))
                .andExpect(jsonPath("$.status", notNullValue()));
    }

    @Test
    void approveBookingWithoutUserTest() throws Exception {
        mvc.perform(patch("/bookings/{bookingId}", 1L)
                        .param("approved", String.valueOf(true))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void approveBookingWithoutApproveParamTest() throws Exception {
        mvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getBookingSuccessTest() throws Exception {
        User itemOwner = makeUser(1L, "name", "email");
        Item item = makeItem(2L, "name", "desc", itemOwner, true);
        User booker = makeUser(3L, "booker", "bookerEmail");
        BookingDtoRequest bookingDtoIn = makeBooking(2L);
        Booking bookingResult = makeBookingResult(4L, item, booker, BookingStatus.WAITING, bookingDtoIn.getStart(), bookingDtoIn.getEnd());

        when(bookingService.get(anyLong(), anyLong())).thenReturn(bookingResult);

        mvc.perform(get("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", booker.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResult.getId()), Long.class))
                .andExpect(jsonPath("$.start", notNullValue()))
                .andExpect(jsonPath("$.end", notNullValue()))
                .andExpect(jsonPath("$.item", notNullValue()))
                .andExpect(jsonPath("$.booker", notNullValue()))
                .andExpect(jsonPath("$.status", notNullValue()));
    }

    @Test
    void getBookingNoUserTest() throws Exception {
        mvc.perform(get("/bookings/{bookingId}", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getAllBookingsByUserTest() throws Exception {
        User itemOwner = makeUser(1L, "name", "email");
        Item item = makeItem(2L, "name", "desc", itemOwner, true);
        User booker = makeUser(3L, "booker", "bookerEmail");
        BookingDtoRequest bookingDtoIn = makeBooking(2L);
        Booking bookingResult1 = makeBookingResult(4L, item, booker, BookingStatus.WAITING, bookingDtoIn.getStart(), bookingDtoIn.getEnd());
        Booking bookingResult2 = makeBookingResult(5L, item, booker, BookingStatus.WAITING, bookingDtoIn.getStart(), bookingDtoIn.getEnd());

        when(userService.getById(anyLong())).thenReturn(booker);
        when(bookingService.getAllBookingsByUser(anyLong(), anyString(), any(), any()))
                .thenReturn(Arrays.asList(bookingResult1, bookingResult2));

        MvcResult mvcResult = mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", booker.getId())
                        .param("state", "ALL")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<BookingDtoResponse> bookings = mapper.readValue(contentAsString, List.class);
        assertThat(bookings.size(), equalTo(2));
    }

    @Test
    void getAllBookingsNoUserTest() throws Exception {
        mvc.perform(get("/bookings")
                        .param("state", "ALL")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getAllBookingsByItemOwnerTest() throws Exception {
        User itemOwner = makeUser(1L, "name", "email");
        Item item = makeItem(2L, "name", "desc", itemOwner, true);
        User booker = makeUser(3L, "booker", "bookerEmail");
        BookingDtoRequest bookingDtoIn = makeBooking(2L);
        Booking bookingResult1 = makeBookingResult(4L, item, booker, BookingStatus.WAITING, bookingDtoIn.getStart(), bookingDtoIn.getEnd());
        Booking bookingResult2 = makeBookingResult(5L, item, booker, BookingStatus.WAITING, bookingDtoIn.getStart(), bookingDtoIn.getEnd());

        when(userService.getById(anyLong())).thenReturn(booker);
        when(bookingService.getAllBookingsByItemOwner(anyLong(), anyString(), any(), any()))
                .thenReturn(Arrays.asList(bookingResult1, bookingResult2));

        MvcResult mvcResult = mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", itemOwner.getId())
                        .param("state", "ALL")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<BookingDtoResponse> bookings = mapper.readValue(contentAsString, List.class);
        assertThat(bookings.size(), equalTo(2));
    }

    @Test
    void getAllBookingsByItemOwnerNoUserTest() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @NoArgsConstructor
    @Getter
    private static class Violations{
        @JsonProperty("violations")
        public List<Violation> violations;

    }

    @NoArgsConstructor
    private static class Violation {
        @JsonProperty("fieldName") public String fieldName;
        @JsonProperty("message") public String message;
    }
}
