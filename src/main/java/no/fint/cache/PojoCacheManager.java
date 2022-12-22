package no.fint.cache;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class PojoCacheManager <T extends Serializable> implements CacheManager<T> {
    private final ConcurrentMap<String, PojoCache<T>> caches = new ConcurrentSkipListMap<>();

    @Override
    public Optional<Cache<T>> getCache(String key) {
        return Optional.ofNullable(caches.get(key));
    }

    @Override
    public Cache<T> createCache(String key) {
        PojoCache<T> newCache = new PojoCache<>();
        PojoCache<T> oldCache = caches.put(key, newCache);
        if (oldCache != null) {
            oldCache.flush();
        }
        return newCache;
    }

    @Override
    public void remove(String key) {
        PojoCache<T> cache = caches.remove(key);
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
