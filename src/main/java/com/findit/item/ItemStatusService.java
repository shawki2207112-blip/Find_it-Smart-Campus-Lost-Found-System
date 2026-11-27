package com.findit.item;

import com.findit.common.ConflictException;
import com.findit.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The ONE place where an item changes status. It validates the transition against {@link ItemStatus} (409 if invalid)
 * and writes an {@link ItemStatusHistory} row for every change.
 */
@Service
@Transactional
public class ItemStatusService {

    private static final Logger log = LoggerFactory.getLogger(ItemStatusService.class);

    private final ItemRepository itemRepository;
    private final ItemStatusHistoryRepository historyRepository;

    public ItemStatusService(ItemRepository itemRepository, ItemStatusHistoryRepository historyRepository) {
        this.itemRepository = itemRepository;
        this.historyRepository = historyRepository;
    }

    /** Records the first history row (null -> REPORTED) of a newly created item. */
    public void recordInitial(Item item, User actor) {
        historyRepository.save(new ItemStatusHistory(item, null, item.getStatus(), actor));
    }

    public Item transition(Item item, ItemStatus target, User actor) {
        ItemStatus current = item.getStatus();
        if (!current.canTransitionTo(target)) {
            throw new ConflictException("Invalid status transition: " + current + " -> " + target);
        }
        item.setStatus(target);
        Item saved = itemRepository.save(item);
        historyRepository.save(new ItemStatusHistory(saved, current, target, actor));
        log.info("Item {} moved {} -> {} by user {}", saved.getId(), current, target, actor.getId());
        return saved;
    }
}
