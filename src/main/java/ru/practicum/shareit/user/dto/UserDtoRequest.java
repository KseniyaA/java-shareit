package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.common.Marker;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
public class UserDtoRequest {
    private Long id;

    @NotBlank(groups = Marker.OnCreate.class,
            message = "Поле email не должно быть пустым")
    @Email(groups = {Marker.OnCreate.class, Marker.OnUpdate.class},
            message = "Не верный формат электронной почты")
    @Size(groups = Marker.OnCreate.class, max = 255)
    private String email;

    @NotBlank(groups = Marker.OnCreate.class,
            message = "Поле name не должно быть пустым")
    @Size(groups = Marker.OnCreate.class, max = 512)
    private String name;
}
