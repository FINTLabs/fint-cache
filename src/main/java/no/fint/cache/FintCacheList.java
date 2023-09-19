package no.fint.cache;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import no.fint.cache.model.CacheObjectType;
import no.fint.cache.model.PojoCacheObject;

import java.io.Serializable;
import java.util.*;

@Slf4j
public class FintCacheList<T extends Serializable> {
    private List<PojoCacheObject<T>> cacheObjects;

    public void updateInternal(Map<String, PojoCacheObject<T>> cacheObjectMap) {
        if (cacheObjects.isEmpty()) {
            log.debug("Empty cache, adding all values");
            cacheObjects = ImmutableList.copyOf(cacheObjectMap.values());
        } else {
            List<PojoCacheObject<T>> cacheObjectsCopy = new ArrayList<>(cacheObjects);
            cacheObjects.forEach(cacheObject -> {
                String checksum = cacheObject.getChecksum();
                if (cacheObjectMap.containsKey(checksum)) {
                    cacheObjectMap.remove(checksum);
                } else {
                    log.debug("Adding new object to the cache (checksum: {})", cacheObject.getChecksum());
                    cacheObjectsCopy.remove(cacheObject);
                }
            });

            cacheObjectsCopy.addAll(cacheObjectMap.values());
            cacheObjects = ImmutableList.sortedCopyOf(Comparator.comparing(CacheObjectType::getChecksum), cacheObjectsCopy);
        }
    }

    public void addInternal(Map<String, PojoCacheObject<T>> newObjects) {
        List<PojoCacheObject<T>> cacheObjectsCopy = new ArrayList<>(cacheObjects);
        cacheObjectsCopy.addAll(newObjects.values());
        cacheObjects = ImmutableList.sortedCopyOf(Comparator.comparing(CacheObjectType::getChecksum), cacheObjectsCopy);
    }

    public void flush() {
        cacheObjects = Collections.emptyList();
    }

    public List<PojoCacheObject<T>> getCacheObjects() {
        return cacheObjects;
    }
}
