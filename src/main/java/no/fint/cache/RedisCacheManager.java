package no.fint.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class RedisCacheManager<T extends Serializable> implements CacheManager<T>{

    private final List<RedisConnectionFactory> redisConnections;
    private final Map<String, RedisCache<T>> redisCacheMap = Collections.synchronizedMap(new HashMap<>());

    public RedisCacheManager(List<RedisConnectionFactory> redisConnections) {
        this.redisConnections = redisConnections;
    }

    @Override
    public Optional<Cache<T>> getCache(String key) {
        return Optional.ofNullable(redisCacheMap.get(key));
    }

    @Override
    public Cache<T> createCache(String key) {
        final RedisConnectionFactory connectionFactory = redisConnections.get(ThreadLocalRandom.current().nextInt(redisConnections.size()));
        RedisTemplate<String,byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.afterPropertiesSet();
        RedisCache<T> cache = new RedisCache<>(key, template);
        redisCacheMap.put(key, cache);
        return cache;
    }

    @Override
    public void remove(String key) {
        log.info("remove({}) not implemented", key);
    }

    @Override
    public boolean hasItems() {
        return !redisCacheMap.isEmpty();
    }

    @Override
    public Set<String> getKeys() {
        return redisCacheMap.keySet();
    }
}
