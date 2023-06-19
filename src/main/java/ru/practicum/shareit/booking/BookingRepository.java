package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long>, QuerydslPredicateExecutor<Booking> {
    List<Booking> findByItem_Id(long itemId);

    List<Booking> findByItemInAndStatusOrderByStartAsc(List<Item> items, BookingStatus status);

    List<Booking> findByItemAndStatusOrderByStartAsc(Item item, BookingStatus status);
}
