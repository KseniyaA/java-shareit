package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.common.Marker;

import javax.validation.constraints.NotBlank;

@Data
@Builder
public class UserDto {
    private Long id;

    @NotBlank(groups = Marker.OnCreate.class,
        message = "Поле email не должно быть пустым")
    private String email;

    @NotBlank(groups = Marker.OnCreate.class,
            message = "Поле name не должно быть пустым")
    private String name;
}
