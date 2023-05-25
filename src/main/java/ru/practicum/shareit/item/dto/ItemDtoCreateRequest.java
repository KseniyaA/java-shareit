package ru.practicum.shareit.item.dto;


import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;

@Data
@Builder
public class ItemDtoCreateRequest {
    @NonNull
    @NotBlank(message = "Name must be not null")
    private String name;
    @NonNull
    private String description;
    @NonNull
    private Boolean available;
}
