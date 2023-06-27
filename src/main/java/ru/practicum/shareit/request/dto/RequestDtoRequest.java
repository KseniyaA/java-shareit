package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.common.Marker;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestDtoRequest {
    private Long id;

    @NotBlank(groups = Marker.OnCreate.class,
            message = "Поле description не должно быть пустым")
    @Size(groups = Marker.OnCreate.class, max = 255)
    private String description;
}
