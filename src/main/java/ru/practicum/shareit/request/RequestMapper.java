package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.user.UserMapper;

public class RequestMapper {
    public static RequestDto toRequestDto(Request request) {
        return RequestDto.builder()
                .description(request.getDescription())
                .requester(UserMapper.toUserDto(request.getRequester()))
                .build();
    }

    public static Request toRequest(RequestDto requestDto) {
        return Request.builder()
                .description(requestDto.getDescription())
                .requester(UserMapper.convertUserDtoToUser(requestDto.getRequester()))
                .id(requestDto.getId())
                .build();
    }
}