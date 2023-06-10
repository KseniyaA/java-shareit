package ru.practicum.shareit.request;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;

@Entity
@Table(name = "requests")
@Data
@Builder
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private User requester;
}
