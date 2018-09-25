package no.fint.cache;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class HazelcastCacheManager<T extends Serializable> implements CacheManager<T> {

    @Autowired
    private HazelcastInstance hazelcast;

    private IMap<String, Cache<T>> caches;

    @Override
    public Optional<Cache<T>> getCache(String key) {
        if (caches.containsKey(key)) {
            return Optional.of(new HazelcastCache<T>(this, key));
        }
        return Optional.empty();
    }

    Cache<T> getCacheInternal(String key) {
        return caches.getOrDefault(key, new FintCache<>());
    }

    @Override
    public Cache<T> createCache(String key) {
        FintCache<T> result = new FintCache<>();
        caches.put(key, result);
        return result;
    }

    @Override
    public void remove(String key) {
        caches.remove(key);
    }

    void replace(String key, Cache<T> replacement) {
        caches.put(key, replacement);
    }

    @Override
    public boolean hasItems() {
        return !caches.isEmpty();
    }

    @Override
    public Set<String> getKeys() {
        return caches.keySet();
    }

    @PostConstruct
    public void init() {
        caches = hazelcast.getMap("fint-caches");
        log.info("Connected to Hazelcast {} with service name {}, {} caches", hazelcast.getName(), caches.getServiceName(), caches.size());
    }
}
