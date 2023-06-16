package ru.practicum.shareit.item.dto;


import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.common.Marker;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class ItemDtoRequest {
    @NotBlank(groups = Marker.OnCreate.class,
            message = "Поле name не должно быть пустым")
    private String name;

    @NotBlank(groups = Marker.OnCreate.class,
            message = "Поле description не должно быть пустым")
    private String description;

    @NotNull(groups = Marker.OnCreate.class,
            message = "Поле available не должно быть пустым")
    private Boolean available;
}
