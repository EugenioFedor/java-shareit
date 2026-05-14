package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByItemId(Long itemId);

    List<Comment> findByItemIdIn(Collection<Long> itemIds);

    default Map<Long, List<Comment>> findCommentsMapByItemIds(Collection<Long> itemIds) {
        return findByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));
    }
}