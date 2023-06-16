package ru.practicum.shareit.item.dto;


import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.common.Marker;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
public class ItemDtoRequest {
    @NotBlank(groups = Marker.OnCreate.class,
            message = "Поле name не должно быть пустым")
    @Size(max = 255, groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private String name;

    @NotBlank(groups = Marker.OnCreate.class,
            message = "Поле description не должно быть пустым")
    @Size(max = 255, groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private String description;

    @NotNull(groups = Marker.OnCreate.class,
            message = "Поле available не должно быть пустым")
    private Boolean available;
}
