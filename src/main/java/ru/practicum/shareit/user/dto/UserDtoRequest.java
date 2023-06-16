package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.common.Marker;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
public class UserDtoRequest {
    private Long id;

    @NotBlank(groups = Marker.OnCreate.class,
            message = "Поле email не должно быть пустым")
    @Email(groups = {Marker.OnCreate.class, Marker.OnUpdate.class} ,
            message = "Не верный формат электронной почты")
    private String email;

    @NotBlank(groups = Marker.OnCreate.class,
            message = "Поле name не должно быть пустым")
    private String name;
}
