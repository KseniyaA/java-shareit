package ru.practicum.shareit.item.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.common.Marker;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class CommentDtoRequest {
    @NotBlank(groups = Marker.OnCreate.class,
            message = "Поле text не должно быть пустым")
    @Size(groups = Marker.OnCreate.class, max = 300)
    private String text;
}
