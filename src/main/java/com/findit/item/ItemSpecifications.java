package com.findit.item;

import com.findit.user.RoleName;
import com.findit.user.User;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/** Reusable JPA Specifications for item search. Every filter is a no-op when its value is missing. */
public final class ItemSpecifications {

    private ItemSpecifications() {
    }

    public static Specification<Item> categoryNameEquals(String name) {
        return (root, query, cb) -> name == null || name.isBlank()
                ? cb.conjunction()
                : cb.equal(cb.lower(root.get("category").get("name")), name.trim().toLowerCase());
    }

    public static Specification<Item> locationContains(String location) {
        return (root, query, cb) -> location == null || location.isBlank()
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("location")), "%" + escapeLike(location.trim().toLowerCase()) + "%", '\\');
    }

    public static Specification<Item> hasType(ItemType type) {
        return (root, query, cb) -> type == null ? cb.conjunction() : cb.equal(root.get("type"), type);
    }

    public static Specification<Item> hasStatus(ItemStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Item> eventDateFrom(LocalDate from) {
        return (root, query, cb) -> from == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.<LocalDate>get("eventDate"), from);
    }

    public static Specification<Item> eventDateTo(LocalDate to) {
        return (root, query, cb) -> to == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.<LocalDate>get("eventDate"), to);
    }

    /**
     * What a user may see in search: ADMIN everything; STUDENT all REPORTED/MATCHED items plus own items;
     * SECURITY_STAFF all FOUND items.
     */
    public static Specification<Item> visibleTo(User actor) {
        return (root, query, cb) -> {
            if (actor.hasRole(RoleName.ROLE_ADMIN)) {
                return cb.conjunction();
            }
            List<Predicate> options = new ArrayList<>();
            options.add(cb.equal(root.get("owner").get("id"), actor.getId()));
            if (actor.hasRole(RoleName.ROLE_STUDENT)) {
                options.add(root.get("status").in(ItemStatus.REPORTED, ItemStatus.MATCHED));
            }
            if (actor.hasRole(RoleName.ROLE_SECURITY_STAFF)) {
                options.add(cb.equal(root.get("type"), ItemType.FOUND));
            }
            return cb.or(options.toArray(new Predicate[0]));
        };
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
