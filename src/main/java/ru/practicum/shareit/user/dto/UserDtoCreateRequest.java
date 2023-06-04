package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDtoCreateRequest {
    private String email;
    private String name;
}
