package no.fint.cache;

import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Slf4j
public class FintCacheManager<T extends Serializable> implements CacheManager<T> {

    private final ConcurrentMap<String, FintCache<T>> caches = new ConcurrentSkipListMap<>();

    @Override
    public Optional<Cache<T>> getCache(String key) {
        return Optional.ofNullable(caches.get(key));
    }

    @Override
    public Cache<T> createCache(String key) {
        FintCache<T> newCache = new FintCache<>();
        FintCache<T> oldCache = caches.put(key, newCache);
        if (oldCache != null) {
            oldCache.flush();
        }
        return newCache;
    }

    @Override
    public void remove(String key) {
        FintCache<T> cache = caches.remove(key);
        if (cache != null) {
            cache.flush();
        }
    }

    @Override
    public boolean hasItems() {
        return !caches.isEmpty();
    }

    @Override
    public Set<String> getKeys() {
        return caches.keySet();
    }
}
