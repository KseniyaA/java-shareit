package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.exception.UnsupportedStatusException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceDbTest {
    private final EntityManager em;
    private final ItemService itemService;
    private final BookingService bookingService;
    private final UserService userService;

    private User makeUser(String name, String email) {
        return User.builder()
                .name(name)
                .email(email)
                .build();
    }

    private Item makeItem(String name, String desc, boolean isAvailable) {
        return Item.builder()
                .name(name)
                .description(desc)
                .available(isAvailable)
                .build();
    }

    private Item makeItem(Long id, String name, String desc, boolean isAvailable) {
        return Item.builder()
                .id(id)
                .name(name)
                .description(desc)
                .available(isAvailable)
                .build();
    }

    private Booking makeBooking(User booker, Item item) {
        return Booking.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .booker(booker)
                .item(item)
                .build();
    }

    @BeforeEach
    private void clear() {
        em.createQuery("delete from Booking").executeUpdate();
        em.createQuery("delete from Item").executeUpdate();
        em.createQuery("delete from User").executeUpdate();
    }

    @Test
    void addBookingTest() {
        User itemOwner = userService.create(makeUser("userName", "userEmail@ya.ru"));
        Item createdItem = itemService.add(makeItem("itemName", "itemDesc", true), itemOwner.getId());

        User booker = userService.create(makeUser("bookerNam2", "bookerEmail@ya.ru"));
        Booking createdBooking = bookingService.create(makeBooking(booker, createdItem), booker);

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b join b.item as i where i.id = :itemId", Booking.class);
        Booking bookingDb = query
                .setParameter("itemId", createdItem.getId())
                .getSingleResult();

        assertThat(createdBooking.getId(), equalTo(bookingDb.getId()));
        assertThat(createdBooking.getStatus(), equalTo(bookingDb.getStatus()));
        assertThat(createdBooking.getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(createdBooking.getBooker().getId(), equalTo(bookingDb.getBooker().getId()));
    }

    @Test
    void deleteBookingTest() {
        User itemOwner = userService.create(makeUser("userName", "userEmail@ya.ru"));
        Item createdItem = itemService.add(makeItem("itemName", "itemDesc", true), itemOwner.getId());

        User booker = userService.create(makeUser("bookerNam2", "bookerEmail@ya.ru"));
        Booking createdBooking = bookingService.create(makeBooking(booker, createdItem), booker);

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.id = :bookingId", Booking.class);
        List<Booking> bookingsDb = query
                .setParameter("bookingId", createdBooking.getId())
                .getResultList();
        assertThat(bookingsDb.size(), equalTo(1));

        bookingService.deleteById(createdBooking.getId());

        query = em.createQuery("Select b from Booking b where b.id = :bookingId", Booking.class);
        bookingsDb = query
                .setParameter("bookingId", createdBooking.getId())
                .getResultList();

        assertThat(bookingsDb.size(), equalTo(0));
    }

    @Test
    void getByIdBookingTest() {
        User itemOwner = userService.create(makeUser("userName", "userEmail@ya.ru"));
        Item createdItem = itemService.add(makeItem("itemName", "itemDesc", true), itemOwner.getId());

        User booker = userService.create(makeUser("bookerNam2", "bookerEmail@ya.ru"));
        Booking createdBooking = bookingService.create(makeBooking(booker, createdItem), booker);

        Booking getByIdBooking = bookingService.getById(createdBooking.getId());

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.id = :bookingId", Booking.class);
        Booking bookingDb = query
                .setParameter("bookingId", createdBooking.getId())
                .getSingleResult();

        assertThat(bookingDb.getStatus(), equalTo(getByIdBooking.getStatus()));
        assertThat(bookingDb.getItem().getId(), equalTo(getByIdBooking.getItem().getId()));
        assertThat(bookingDb.getBooker().getId(), equalTo(getByIdBooking.getBooker().getId()));
        assertThat(bookingDb.getStart(), equalTo(getByIdBooking.getStart()));
        assertThat(bookingDb.getEnd(), equalTo(getByIdBooking.getEnd()));
    }


    @Test
    void approveBookingTest() {
        User itemOwner = userService.create(makeUser("userName", "userEmail@ya.ru"));
        Item createdItem = itemService.add(makeItem("itemName", "itemDesc", true), itemOwner.getId());
        User booker = userService.create(makeUser("bookerNam2", "bookerEmail@ya.ru"));
        Booking createdBooking = bookingService.create(makeBooking(booker, createdItem), booker);

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.id = :bookingId", Booking.class);
        Booking bookingDb = query
                .setParameter("bookingId", createdBooking.getId())
                .getSingleResult();
        assertThat(bookingDb.getStatus(), equalTo(BookingStatus.WAITING));

        Booking approvedBooking = bookingService.approve(createdBooking.getId(), true, itemOwner.getId());

        query = em.createQuery("Select b from Booking b where b.id = :bookingId", Booking.class);
        bookingDb = query
                .setParameter("bookingId", createdBooking.getId())
                .getSingleResult();

        assertThat(approvedBooking.getStatus(), equalTo(bookingDb.getStatus()));
        assertThat(bookingDb.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void getBookingSuccessTest() {
        User itemOwner = userService.create(makeUser("userName", "userEmail@ya.ru"));
        Item createdItem = itemService.add(makeItem("itemName", "itemDesc", true), itemOwner.getId());

        User booker = userService.create(makeUser("bookerNam2", "bookerEmail@ya.ru"));
        Booking createdBooking = bookingService.create(makeBooking(booker, createdItem), booker);

        Booking getByIdBooking = bookingService.get(createdBooking.getId(), booker.getId());

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.id = :bookingId", Booking.class);
        Booking bookingDb = query
                .setParameter("bookingId", createdBooking.getId())
                .getSingleResult();

        assertThat(bookingDb.getStatus(), equalTo(getByIdBooking.getStatus()));
        assertThat(bookingDb.getItem().getId(), equalTo(getByIdBooking.getItem().getId()));
        assertThat(bookingDb.getBooker().getId(), equalTo(getByIdBooking.getBooker().getId()));
        assertThat(bookingDb.getStart(), equalTo(getByIdBooking.getStart()));
        assertThat(bookingDb.getEnd(), equalTo(getByIdBooking.getEnd()));
    }

    @Test
    void getAllBookingsByItemOwnerALLTest() {
        User itemOwner = userService.create(makeUser("userName", "userEmail@ya.ru"));
        Item createdItem = itemService.add(makeItem("itemName", "itemDesc", true), itemOwner.getId());
        User booker = userService.create(makeUser("bookerNam2", "bookerEmail@ya.ru"));
        Booking bookingDto1 = makeBooking(booker, createdItem);
        bookingDto1.setStart(LocalDateTime.now().plusMinutes(100));
        bookingDto1.setEnd(LocalDateTime.now().plusMinutes(200));

        Booking bookingDto2 = makeBooking(booker, createdItem);
        bookingDto2.setStart(LocalDateTime.now().plusMinutes(10));
        bookingDto2.setEnd(LocalDateTime.now().plusMinutes(20));

        Booking createdBooking1 = bookingService.create(bookingDto1, booker);
        Booking createdBooking2 = bookingService.create(bookingDto2, booker);

        List<Booking> byService = bookingService.getAllBookingsByItemOwner(itemOwner.getId(), "ALL", null, null);
        Query nativeQuery = em.createNativeQuery("Select b.* from bookings as b " +
                "join items as i on b.item_id = i.id " +
                "join users as u on u.id = i.owner_id " +
                "where u.id = :ownerId " +
                "order by b.start_date desc ", Booking.class);
        List<Booking> byDb = nativeQuery
                .setParameter("ownerId", itemOwner.getId())
                .getResultList();

        assertThat(byService.size(), equalTo(byDb.size()));
        assertThat(byService.size(), equalTo(2));
        assertThat(byService.get(0), equalTo(byDb.get(0)));
        assertThat(byService.get(1), equalTo(byDb.get(1)));
        assertThat(byService.get(0).getId(), equalTo(createdBooking1.getId()));

    }

    @Test
    void getAllBookingsByItemOwnerCURRENTTest() {
        User itemOwner = userService.create(makeUser("userName", "userEmail@ya.ru"));
        Item createdItem = itemService.add(makeItem("itemName", "itemDesc", true), itemOwner.getId());
        Item createdItem2 = itemService.add(makeItem("itemName2", "itemDesc2", true), itemOwner.getId());
        Item createdItem3 = itemService.add(makeItem("itemName3", "itemDesc3", true), itemOwner.getId());
        Item createdItem4 = itemService.add(makeItem("itemName4", "itemDesc4", true), itemOwner.getId());

        User booker = userService.create(makeUser("bookerNam2", "bookerEmail@ya.ru"));
        Booking currentDto1 = makeBooking(booker, createdItem);
        currentDto1.setStart(LocalDateTime.now().minusMinutes(10));
        currentDto1.setEnd(LocalDateTime.now().plusMinutes(20));

        Booking currentDto2 = makeBooking(booker, createdItem2);
        currentDto2.setStart(LocalDateTime.now().minusMinutes(20));
        currentDto2.setEnd(LocalDateTime.now().plusMinutes(30));

        Booking currentDto3 = makeBooking(booker, createdItem3);
        currentDto3.setStart(LocalDateTime.now().minusMinutes(5));
        currentDto3.setEnd(LocalDateTime.now().plusMinutes(10));

        Booking futureDto4 = makeBooking(booker, createdItem4);
        futureDto4.setStart(LocalDateTime.now().plusMinutes(100));
        futureDto4.setEnd(LocalDateTime.now().plusMinutes(200));

        Booking pastDto5 = makeBooking(booker, createdItem4);
        pastDto5.setStart(LocalDateTime.now().minusMinutes(200));
        pastDto5.setEnd(LocalDateTime.now().minusMinutes(150));

        Booking createdBooking1 = bookingService.create(currentDto1, booker);
        Booking createdBooking2 = bookingService.create(currentDto2, booker);
        Booking createdBooking3 = bookingService.create(currentDto3, booker);
        Booking futureBooking4 = bookingService.create(futureDto4, booker);
        Booking pastBooking4 = bookingService.create(pastDto5, booker);

        List<Booking> byService = bookingService.getAllBookingsByItemOwner(itemOwner.getId(), "CURRENT", null, null);
        Query nativeQuery = em.createNativeQuery("Select b.* from bookings as b " +
                "join items as i on b.item_id = i.id " +
                "join users as u on u.id = i.owner_id " +
                "where u.id = :ownerId " +
                "and b.start_date <= :startDate " +
                "and b.end_date >= :endDate " +
                "order by b.start_date asc ", Booking.class);
        List<Booking> byDb = nativeQuery
                .setParameter("ownerId", itemOwner.getId())
                .setParameter("startDate", LocalDateTime.now())
                .setParameter("endDate", LocalDateTime.now())
                .getResultList();

        assertThat(byService.size(), equalTo(3));
        assertThat(byService.size(), equalTo(byDb.size()));

        assertThat(byService.get(0).getId(), equalTo(currentDto2.getId()));
        assertThat(byService.get(1).getId(), equalTo(currentDto1.getId()));
        assertThat(byService.get(2).getId(), equalTo(currentDto3.getId()));
        assertThat(byService.get(0), equalTo(byDb.get(0)));
        assertThat(byService.get(1), equalTo(byDb.get(1)));
        assertThat(byService.get(2), equalTo(byDb.get(2)));
    }

    @Test
    void getAllBookingsByItemOwnerPASTTest() {
        User itemOwner = userService.create(makeUser("userName", "userEmail@ya.ru"));
        Item createdItem = itemService.add(makeItem("itemName", "itemDesc", true), itemOwner.getId());
        Item createdItem2 = itemService.add(makeItem("itemName2", "itemDesc2", true), itemOwner.getId());
        Item createdItem3 = itemService.add(makeItem("itemName3", "itemDesc3", true), itemOwner.getId());
        Item createdItem4 = itemService.add(makeItem("itemName4", "itemDesc4", true), itemOwner.getId());

        User booker = userService.create(makeUser("bookerNam2", "bookerEmail@ya.ru"));
        Booking currentDto1 = makeBooking(booker, createdItem);
        currentDto1.setStart(LocalDateTime.now().minusMinutes(10));
        currentDto1.setEnd(LocalDateTime.now().plusMinutes(20));

        Booking currentDto2 = makeBooking(booker, createdItem2);
        currentDto2.setStart(LocalDateTime.now().minusMinutes(20));
        currentDto2.setEnd(LocalDateTime.now().plusMinutes(30));

        Booking currentDto3 = makeBooking(booker, createdItem3);
        currentDto3.setStart(LocalDateTime.now().minusMinutes(5));
        currentDto3.setEnd(LocalDateTime.now().plusMinutes(10));

        Booking futureDto4 = makeBooking(booker, createdItem4);
        futureDto4.setStart(LocalDateTime.now().plusMinutes(100));
        futureDto4.setEnd(LocalDateTime.now().plusMinutes(200));

        Booking pastDto5 = makeBooking(booker, createdItem4);
        pastDto5.setStart(LocalDateTime.now().minusMinutes(200));
        pastDto5.setEnd(LocalDateTime.now().minusMinutes(150));

        Booking pastDto6 = makeBooking(booker, createdItem3);
        pastDto6.setStart(LocalDateTime.now().minusMinutes(250));
        pastDto6.setEnd(LocalDateTime.now().minusMinutes(190));

        Booking createdBooking1 = bookingService.create(currentDto1, booker);
        Booking createdBooking2 = bookingService.create(currentDto2, booker);
        Booking futureBooking4 = bookingService.create(futureDto4, booker);
        Booking pastBooking5 = bookingService.create(pastDto5, booker);
        Booking pastBooking6 = bookingService.create(pastDto6, booker);

        List<Booking> byService = bookingService.getAllBookingsByItemOwner(itemOwner.getId(), "PAST", null, null);
        Query nativeQuery = em.createNativeQuery("Select b.* from bookings as b " +
                "join items as i on b.item_id = i.id " +
                "join users as u on u.id = i.owner_id " +
                "where u.id = :ownerId " +
                "and b.end_date <= :endDate " +
                "order by b.start_date desc ", Booking.class);
        List<Booking> byDb = nativeQuery
                .setParameter("ownerId", itemOwner.getId())
                .setParameter("endDate", LocalDateTime.now())
                .getResultList();

        assertThat(byService.size(), equalTo(2));
        assertThat(byService.size(), equalTo(byDb.size()));

        assertThat(byService.get(0).getId(), equalTo(pastBooking5.getId()));
        assertThat(byService.get(1).getId(), equalTo(pastBooking6.getId()));
        assertThat(byService.get(0), equalTo(byDb.get(0)));
        assertThat(byService.get(1), equalTo(byDb.get(1)));
    }

    @Test
    void getAllBookingsByItemOwnerFUTURETest() {
        User itemOwner = userService.create(makeUser("userName", "userEmail@ya.ru"));
        Item createdItem = itemService.add(makeItem("itemName", "itemDesc", true), itemOwner.getId());
        Item createdItem2 = itemService.add(makeItem("itemName2", "itemDesc2", true), itemOwner.getId());
        Item createdItem3 = itemService.add(makeItem("itemName3", "itemDesc3", true), itemOwner.getId());
        Item createdItem4 = itemService.add(makeItem("itemName4", "itemDesc4", true), itemOwner.getId());

        User booker = userService.create(makeUser("bookerNam2", "bookerEmail@ya.ru"));
        Booking currentDto1 = makeBooking(booker, createdItem);
        currentDto1.setStart(LocalDateTime.now().minusMinutes(10));
        currentDto1.setEnd(LocalDateTime.now().plusMinutes(20));

        Booking currentDto2 = makeBooking(booker, createdItem2);
        currentDto2.setStart(LocalDateTime.now().minusMinutes(20));
        currentDto2.setEnd(LocalDateTime.now().plusMinutes(30));

        Booking currentDto3 = makeBooking(booker, createdItem3);
        currentDto3.setStart(LocalDateTime.now().minusMinutes(5));
        currentDto3.setEnd(LocalDateTime.now().plusMinutes(10));

        Booking futureDto4 = makeBooking(booker, createdItem4);
        futureDto4.setStart(LocalDateTime.now().plusMinutes(100));
        futureDto4.setEnd(LocalDateTime.now().plusMinutes(200));

        Booking pastDto5 = makeBooking(booker, createdItem4);
        pastDto5.setStart(LocalDateTime.now().minusMinutes(200));
        pastDto5.setEnd(LocalDateTime.now().minusMinutes(150));

        Booking pastDto6 = makeBooking(booker, createdItem3);
        pastDto6.setStart(LocalDateTime.now().minusMinutes(250));
        pastDto6.setEnd(LocalDateTime.now().minusMinutes(190));

        Booking createdBooking1 = bookingService.create(currentDto1, booker);
        Booking createdBooking2 = bookingService.create(currentDto2, booker);
        Booking futureBooking4 = bookingService.create(futureDto4, booker);
        Booking pastBooking5 = bookingService.create(pastDto5, booker);
        Booking pastBooking6 = bookingService.create(pastDto6, booker);

        List<Booking> byService = bookingService.getAllBookingsByItemOwner(itemOwner.getId(), "FUTURE", null, null);
        Query nativeQuery = em.createNativeQuery("Select b.* from bookings as b " +
                "join items as i on b.item_id = i.id " +
                "join users as u on u.id = i.owner_id " +
                "where u.id = :ownerId " +
                "and b.start_date >= :startDate " +
                "order by b.start_date desc ", Booking.class);
        List<Booking> byDb = nativeQuery
                .setParameter("ownerId", itemOwner.getId())
                .setParameter("startDate", LocalDateTime.now())
                .getResultList();

        assertThat(byService.size(), equalTo(1));
        assertThat(byService.size(), equalTo(byDb.size()));

        assertThat(byService.get(0).getId(), equalTo(futureBooking4.getId()));
        assertThat(byService.get(0), equalTo(byDb.get(0)));
    }

    @Test
    void getAllBookingsByItemOwnerWaitingRejectedTest() {
        User itemOwner = userService.create(makeUser("userName", "userEmail@ya.ru"));
        Item createdItem = itemService.add(makeItem("itemName", "itemDesc", true), itemOwner.getId());
        Item createdItem2 = itemService.add(makeItem("itemName2", "itemDesc2", true), itemOwner.getId());
        Item createdItem3 = itemService.add(makeItem("itemName3", "itemDesc3", true), itemOwner.getId());
        Item createdItem4 = itemService.add(makeItem("itemName4", "itemDesc4", true), itemOwner.getId());

        User booker = userService.create(makeUser("bookerNam2", "bookerEmail@ya.ru"));
        Booking currentDto1 = makeBooking(booker, createdItem);
        currentDto1.setStart(LocalDateTime.now().minusMinutes(10));
        currentDto1.setEnd(LocalDateTime.now().plusMinutes(20));

        Booking currentDto2 = makeBooking(booker, createdItem2);
        currentDto2.setStart(LocalDateTime.now().minusMinutes(20));
        currentDto2.setEnd(LocalDateTime.now().plusMinutes(30));

        Booking currentDto3 = makeBooking(booker, createdItem3);
        currentDto3.setStart(LocalDateTime.now().minusMinutes(5));
        currentDto3.setEnd(LocalDateTime.now().plusMinutes(10));

        Booking futureDto4 = makeBooking(booker, createdItem4);
        futureDto4.setStart(LocalDateTime.now().plusMinutes(100));
        futureDto4.setEnd(LocalDateTime.now().plusMinutes(200));

        Booking pastDto5 = makeBooking(booker, createdItem4);
        pastDto5.setStart(LocalDateTime.now().minusMinutes(200));
        pastDto5.setEnd(LocalDateTime.now().minusMinutes(150));

        Booking pastDto6 = makeBooking(booker, createdItem3);
        pastDto6.setStart(LocalDateTime.now().minusMinutes(250));
        pastDto6.setEnd(LocalDateTime.now().minusMinutes(190));

        Booking createdBooking1 = bookingService.create(currentDto1, booker);
        bookingService.approve(createdBooking1.getId(), true, itemOwner.getId());
        Booking createdBooking2 = bookingService.create(currentDto2, booker);
        Booking futureBooking4 = bookingService.create(futureDto4, booker);
        Booking pastBooking5 = bookingService.create(pastDto5, booker);
        Booking pastBooking6 = bookingService.create(pastDto6, booker);
        bookingService.approve(pastBooking6.getId(), false, itemOwner.getId());

        List<Booking> byService = bookingService.getAllBookingsByItemOwner(itemOwner.getId(), "WAITING", null, null);
        Query nativeQuery = em.createNativeQuery("Select b.* from bookings as b " +
                "join items as i on b.item_id = i.id " +
                "join users as u on u.id = i.owner_id " +
                "where u.id = :ownerId " +
                "and b.status = 'WAITING' " +
                "order by b.start_date desc ", Booking.class);
        List<Booking> byDb = nativeQuery
                .setParameter("ownerId", itemOwner.getId())
                .getResultList();

        assertThat(byService.size(), equalTo(3));
        assertThat(byService.size(), equalTo(byDb.size()));

        assertThat(byService.get(0).getId(), equalTo(futureBooking4.getId()));
        assertThat(byService.get(1).getId(), equalTo(createdBooking2.getId()));
        assertThat(byService.get(2).getId(), equalTo(pastBooking5.getId()));
        assertThat(byService.get(0), equalTo(byDb.get(0)));
        assertThat(byService.get(1), equalTo(byDb.get(1)));
        assertThat(byService.get(2), equalTo(byDb.get(2)));

        // rejected
        byService = bookingService.getAllBookingsByItemOwner(itemOwner.getId(), "REJECTED", null, null);
        nativeQuery = em.createNativeQuery("Select b.* from bookings as b " +
                "join items as i on b.item_id = i.id " +
                "join users as u on u.id = i.owner_id " +
                "where u.id = :ownerId " +
                "and b.status = 'REJECTED' " +
                "order by b.start_date desc ", Booking.class);
        byDb = nativeQuery
                .setParameter("ownerId", itemOwner.getId())
                .getResultList();

        assertThat(byService.size(), equalTo(1));
        assertThat(byService.size(), equalTo(byDb.size()));

        assertThat(byService.get(0).getId(), equalTo(pastBooking6.getId()));
    }

    @Test
    void getAllBookingsByItemOwnerUnknownStatusTest() {
        User itemOwner = userService.create(makeUser("userName", "userEmail@ya.ru"));

        UnsupportedStatusException exception = Assertions.assertThrows(
                UnsupportedStatusException.class,
                () -> bookingService.getAllBookingsByItemOwner(itemOwner.getId(), "unknown", null, null));

        assertThat(exception.getMessage(), equalTo("Unknown state: UNSUPPORTED_STATUS"));
    }
}
