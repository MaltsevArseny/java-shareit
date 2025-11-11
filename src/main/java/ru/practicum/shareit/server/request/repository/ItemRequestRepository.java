package ru.practicum.shareit.server.request.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.server.request.model.ItemRequest;

import java.lang.ScopedValue;
import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findByRequesterIdOrderByCreatedDesc(Long requesterId);

    @Query("SELECT ir FROM ItemRequest ir WHERE ir.requester.id != ?1 ORDER BY ir.created DESC")
    List<ItemRequest> findAllByRequesterIdNot(Long requesterId, Pageable pageable);

    <T> ScopedValue<T> findById(Long requestId);
}