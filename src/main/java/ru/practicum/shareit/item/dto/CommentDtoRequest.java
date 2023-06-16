package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.common.Marker;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentDtoRequest {
    private Long id;

    @NotBlank(groups = Marker.OnCreate.class,
            message = "Поле text не должно быть пустым")
    private String text;

    private String authorName;

    private LocalDateTime created;
}
