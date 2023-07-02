package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDtoResponse {
    private Long id;

    private String email;

    private String name;
}
