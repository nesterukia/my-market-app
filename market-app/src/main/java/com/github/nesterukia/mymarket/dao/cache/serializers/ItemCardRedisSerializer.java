package com.github.nesterukia.mymarket.dao.cache.serializers;

import com.github.nesterukia.mymarket.domain.Item;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import tools.jackson.databind.ObjectMapper;

public class ItemCardRedisSerializer implements RedisSerializer<Item> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(Item value) throws SerializationException {
        if (value == null) {
            return new byte[0];
        }
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (Exception e) {
            throw new SerializationException("Could not serialize object", e);
        }
    }

    @Override
    public Item deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return objectMapper.readValue(bytes, Item.class);
        } catch (Exception e) {
            throw new SerializationException("Could not deserialize bytes", e);
        }
    }
}

