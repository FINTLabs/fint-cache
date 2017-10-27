package no.fint.cache;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

public interface CacheManager<T extends Serializable> {

    Optional<Cache<T>> getCache(String key);

        //         return Optional.ofNullable(caches.get());


    Cache<T> createCache(String key);
        /*
        Cache<T> cache = cacheSupplier.get();
        caches.put(, cache);
        return cache;

         */

    void remove(String key);
        /*
        Optional<Cache<T>> cache = getCache(orgId);
        cache.ifPresent(c -> {
            c.flush();
            caches.remove(orgId);
        });

         */

    boolean hasItems();

    Set<String> getKeys();
}
