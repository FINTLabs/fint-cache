package no.fint.cache;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

public interface CacheManager<T extends Serializable> {

    Optional<Cache<T>> getCache(String key);

    Cache<T> createCache(String key);

    void remove(String key);

    boolean hasItems();

    Set<String> getKeys();
}
