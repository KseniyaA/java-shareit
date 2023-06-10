package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long>, QuerydslPredicateExecutor<Booking> {
    List<Booking> findByItem_Id(long itemId);
}
