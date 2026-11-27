package com.findit.item;

import com.findit.admin.CategoryCount;
import com.findit.category.Category;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    @Override
    @EntityGraph(attributePaths = {"category", "owner"})
    Page<Item> findAll(Specification<Item> spec, Pageable pageable);

    List<Item> findByTypeAndCategoryAndStatusIn(ItemType type, Category category, Collection<ItemStatus> statuses);

    List<Item> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    List<Item> findTop6ByStatusInOrderByCreatedAtDesc(Collection<ItemStatus> statuses);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByOwnerId(Long ownerId);

    long countByStatus(ItemStatus status);

    long countByStatusNot(ItemStatus status);

    @Query("select new com.findit.admin.CategoryCount(c.name, count(i)) from Item i join i.category c "
            + "where i.type = :type group by c.name order by count(i) desc, c.name asc")
    List<CategoryCount> findTopCategoriesByType(@Param("type") ItemType type, Pageable pageable);
}
