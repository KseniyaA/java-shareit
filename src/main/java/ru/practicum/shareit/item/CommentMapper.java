package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CommentMapper {
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    
    public static Comment toComment(CommentDto commentDto) {
        return Comment.builder()
                .text(commentDto.getText())
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
                .withZone(ZoneId.of("GMT"));
        String created = formatter.format(comment.getCreated());

        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(created)
                .build();
    }
}
