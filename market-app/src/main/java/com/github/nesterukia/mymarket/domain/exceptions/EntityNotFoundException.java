package com.github.nesterukia.mymarket.domain.exceptions;

import com.github.nesterukia.mymarket.utils.EntityType;

public class EntityNotFoundException extends RuntimeException {
    private static final String ENTITY_WITH_ID_NOT_FOUND = "%s with id = '%d' wasn't found.";
    private static final String ITEM_IN_CART_NOT_FOUND = "Item with id = '%d' wasn't found in cart with id = '%d'.";

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(EntityType entityType, Long id) {
        super(ENTITY_WITH_ID_NOT_FOUND.formatted(entityType.name(), id));
    }

    public static EntityNotFoundException itemInCartNotFound(Long itemId, Long cartId) {
        return new EntityNotFoundException(ITEM_IN_CART_NOT_FOUND.formatted(itemId, cartId));
    }
}
