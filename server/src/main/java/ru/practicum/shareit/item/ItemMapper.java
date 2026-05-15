package ru.practicum.shareit.item;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ItemMapper {

    @Mapping(source = "request.id", target = "requestId")
    ItemDto toItemDto(Item item);

    @Mapping(target = "request", ignore = true)
    Item toItem(ItemDto itemDto);

    @Mapping(source = "author.name", target = "authorName")
    @Mapping(source = "created", target = "created")
    CommentDto toCommentDto(Comment comment);

    default Comment toComment(CommentDto dto, Item item, User author) {
        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        return comment;
    }
}