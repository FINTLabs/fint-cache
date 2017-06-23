package no.fint.cache;

import lombok.extern.slf4j.Slf4j;
import no.fint.cache.model.CacheObject;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class CacheService<T> {

    private Map<String, Cache<T>> caches = new HashMap<>();

    public Set<String> getKeys() {
        return caches.keySet();
    }

    public Cache<T> createCache(String cacheUri) {
        FintCache<T> cache = new FintCache<>();
        caches.put(cacheUri, cache);
        return cache;
    }

    public void put(String cacheUri, FintCache<T> cache) {
        caches.put(cacheUri, cache);
    }

    @SuppressWarnings("unchecked")
    public long getLastUpdated(String cacheUri) {
        FintCache<T> fintCache = (FintCache) caches.get(cacheUri);
        return fintCache.getLastUpdated();
    }

    public Optional<Cache<T>> getCache(String cacheUri) {
        if (cacheUri == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(caches.get(cacheUri));
    }

    public List<T> getAll(String cacheUri) {
        Optional<Cache<T>> cache = getCache(cacheUri);
        if (cache.isPresent()) {
            List<CacheObject<T>> cacheObjects = cache.get().get();
            return cacheObjects.stream().map(CacheObject::getObject).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public List<T> getAll(String cacheUri, long sinceTimestamp) {
        Optional<Cache<T>> cache = getCache(cacheUri);
        if (cache.isPresent()) {
            List<CacheObject<T>> cacheObjects = cache.get().getSince(sinceTimestamp);
            return cacheObjects.stream().map(CacheObject::getObject).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public void update(String cacheUri, List<T> objects) {
        Optional<Cache<T>> cache = getCache(cacheUri);
        cache.ifPresent(c -> c.update(objects));
    }

    public void add(String cacheUri, List<T> objects) {
        Optional<Cache<T>> cache = getCache(cacheUri);
        cache.ifPresent(c -> c.add(objects));
    }

    public void flush(String cacheUri) {
        Optional<Cache<T>> cache = getCache(cacheUri);
        cache.ifPresent(Cache::flush);
    }

    public void remove(String cacheUri) {
        Optional<Cache<T>> cache = getCache(cacheUri);
        cache.ifPresent(c -> {
            c.flush();
            caches.remove(cacheUri);
        });
    }

}
