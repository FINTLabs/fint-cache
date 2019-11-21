package no.fint.cache;

import lombok.extern.slf4j.Slf4j;
import no.fint.cache.model.CacheObject;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Slf4j
public class HazelcastCache<T extends Serializable> implements Cache<T> {

    private final HazelcastCacheManager<T> manager;
    private final String key;

    HazelcastCache(HazelcastCacheManager<T> manager, String key) {
        this.manager = manager;
        this.key = key;
    }

    @Override
    public void update(List<T> objects) {
        Cache<T> cache = manager.getCacheInternal(key);
        cache.update(objects);
        manager.replace(key, cache);
    }

    @Override
    public void updateCache(List<CacheObject<T>> objects) {
        Cache<T> cache = manager.getCacheInternal(key);
        cache.updateCache(objects);
        manager.replace(key, cache);
    }

    @Override
    public void add(List<T> objects) {
        Cache<T> cache = manager.getCacheInternal(key);
        cache.add(objects);
        manager.replace(key, cache);
    }

    @Override
    public void addCache(List<CacheObject<T>> objects) {
        Cache<T> cache = manager.getCacheInternal(key);
        cache.addCache(objects);
        manager.replace(key, cache);
    }

    @Override
    public void flush() {
        Cache<T> cache = manager.getCacheInternal(key);
        cache.flush();
        manager.replace(key, cache);
    }

    @Override
    public Stream<CacheObject<T>> get() {
        return manager.getCacheInternal(key).get();
    }

    @Override
    public Stream<CacheObject<T>> getSince(long timestamp) {
        return manager.getCacheInternal(key).getSince(timestamp);
    }

    @Override
    public long getLastUpdated() {
        return manager.getCacheInternal(key).getLastUpdated();
    }

    @Override
    public int size() {
        return manager.getCacheInternal(key).size();
    }

    @Override
    public long volume() { return manager.getCacheInternal(key).volume(); }

    @Override
    public Stream<CacheObject<T>> filter(Predicate<T> predicate) {
        return manager.getCacheInternal(key).filter(predicate);
    }

    @Override
    public Stream<CacheObject<T>> filter(int hashCode, Predicate<T> predicate) {
        return manager.getCacheInternal(key).filter(hashCode, predicate);
    }

}
