package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.Request;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query(" select i from Item i " +
            "where (upper(i.name) like upper(concat('%', ?1, '%')) " +
            " or upper(i.description) like upper(concat('%', ?1, '%'))) " +
            " and i.available = true")
    List<Item> searchByText(String text);

    @Query(" select i from Item i " +
            "where (upper(i.name) like upper(concat('%', ?1, '%')) " +
            " or upper(i.description) like upper(concat('%', ?1, '%'))) " +
            " and i.available = true")
    Page<Item> searchByText(String text, Pageable page);

    @Query(value = " select i from Item i join i.owner as u " +
            "where u.id = ?1 order by i.id asc")
    Page<Item> findAllByOwnerId(long ownerId, Pageable page);

    @Query(" select i from Item i join i.owner as u " +
            "where u.id = ?1 order by i.id asc")
    List<Item> findAllByOwnerId(long ownerId);

    List<Item> findByRequestIn(List<Request> requests);

    List<Item> findByRequest(Request requests);
}
