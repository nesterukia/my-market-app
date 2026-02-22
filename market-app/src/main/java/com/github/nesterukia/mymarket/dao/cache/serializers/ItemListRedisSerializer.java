package com.github.nesterukia.mymarket.dao.cache.serializers;

import com.github.nesterukia.mymarket.domain.Item;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

public class ItemListRedisSerializer implements RedisSerializer<List<Item>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(List<Item> value) throws SerializationException {
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
    public List<Item> deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return List.of(objectMapper.readValue(bytes, Item[].class));
        } catch (Exception e) {
            throw new SerializationException("Could not deserialize bytes", e);
        }
    }
}


