package com.findit;

import com.findit.category.Category;
import com.findit.item.Item;
import com.findit.item.ItemStatus;
import com.findit.item.ItemType;
import com.findit.user.Role;
import com.findit.user.RoleName;
import com.findit.user.User;
import java.time.LocalDate;
import java.util.HashSet;

/** Small factory methods that build entities for unit tests. */
public final class TestData {

    private TestData() {
    }

    public static User user(Long id, String name, RoleName... roles) {
        User user = new User(name, name.toLowerCase().replace(' ', '.') + "@test.local", "hash");
        user.setId(id);
        user.setRoles(new HashSet<>());
        for (RoleName role : roles) {
            user.getRoles().add(new Role(role));
        }
        return user;
    }

    public static Category category(Long id, String name) {
        Category category = new Category(name);
        category.setId(id);
        return category;
    }

    public static Item item(Long id, ItemType type, String title, String description, Category category,
                            String location, LocalDate date, User owner, ItemStatus status) {
        Item item = new Item();
        item.setId(id);
        item.setType(type);
        item.setTitle(title);
        item.setDescription(description);
        item.setCategory(category);
        item.setLocation(location);
        item.setEventDate(date);
        item.setOwner(owner);
        item.setStatus(status);
        return item;
    }
}
