package com.findit.claim;

import com.findit.item.Item;
import com.findit.user.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    boolean existsByItemAndClaimantAndStatus(Item item, User claimant, ClaimStatus status);

    boolean existsByClaimantId(Long claimantId);

    List<Claim> findByClaimantIdOrderByCreatedAtDesc(Long claimantId);

    List<Claim> findByStatusOrderByCreatedAtAsc(ClaimStatus status);

    @Modifying
    @Query("delete from Claim c where c.item.id = :itemId")
    void deleteByItemId(@Param("itemId") Long itemId);
}
