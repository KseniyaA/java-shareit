package ru.practicum.shareit.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    @Query(" select r from Request r join r.requester as u " +
            "where u.id = ?1")
    List<Request> findAllByRequesterId(long requesterId);

    @Query(value = " select r.* from requests as r " +
            "join items as i on r.id = i.request_id  " +
            "join users as u on i.owner_id = u.id " +
            "where u.id = ?1", nativeQuery = true)
    Page<Request> findAll(long requesterId, Pageable page);
}
