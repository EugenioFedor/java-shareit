package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByOwner_Id(Long ownerId);

    @Query("""
            select i
            from Item i
            where i.available = true
            and (
                upper(i.name) like upper(concat('%', ?1, '%'))
                or upper(i.description) like upper(concat('%', ?1, '%'))
            )
            """)
    List<Item> search(String text);

    List<Item> findAllByRequest_Id(Long requestId);

    List<Item> findAllByRequest_IdIn(List<Long> requestIds);
}