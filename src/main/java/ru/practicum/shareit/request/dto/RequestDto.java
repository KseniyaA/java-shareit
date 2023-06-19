package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.common.Marker;
import ru.practicum.shareit.user.dto.UserDtoRequest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
public class RequestDto {
    private Long id;

    @NotBlank(groups = Marker.OnCreate.class,
            message = "Поле description не должно быть пустым")
    @Size(groups = Marker.OnCreate.class, max = 255)
    private String description;

    @NotBlank(groups = Marker.OnCreate.class,
            message = "Поле requester не должно быть пустым")
    private UserDtoRequest requester;
}
