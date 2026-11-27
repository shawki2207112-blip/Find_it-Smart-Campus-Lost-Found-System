package com.findit.item;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemStatusHistoryRepository extends JpaRepository<ItemStatusHistory, Long> {

    List<ItemStatusHistory> findByItemIdOrderByChangedAtAscIdAsc(Long itemId);

    @Modifying
    @Query("delete from ItemStatusHistory h where h.item.id = :itemId")
    void deleteByItemId(@Param("itemId") Long itemId);
}
