package no.fint.cache;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.fint.cache.model.CacheMetaData;
import no.fint.cache.model.CacheObject;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class FintCache<T extends Serializable> implements Cache<T>, Serializable {
    @Getter
    private CacheMetaData cacheMetaData;
    private Set<CacheObject<T>> cacheObjects;

    public FintCache() {
        cacheMetaData = new CacheMetaData();
        cacheObjects = Collections.emptySet();
    }

    @Override
    public void update(List<T> objects) {
        if (objects.isEmpty()) {
            log.debug("Empty list sent in, will not update cache");
        } else {
            Map<String, CacheObject<T>> cacheObjectMap = getMap(objects);
            if (cacheObjects.isEmpty()) {
                log.debug("Empty cache, adding all values");
                cacheObjects = new HashSet<>(cacheObjectMap.values());
            } else {
                Set<CacheObject<T>> cacheObjectsCopy = new HashSet<>(cacheObjects);
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
                cacheObjects = cacheObjectsCopy;
            }

            updateMetaData();
        }
    }

    @Override
    public void add(List<T> objects) {
        Map<String, CacheObject<T>> newObjects = getMap(objects);
        Set<CacheObject<T>> cacheObjectsCopy = new HashSet<>(cacheObjects);
        cacheObjectsCopy.addAll(newObjects.values());
        cacheObjects = cacheObjectsCopy;
        updateMetaData();
    }


    @Override
    public void flush() {
        flushMetaData();
        cacheObjects = Collections.emptySet();
    }

    @Override
    public Stream<CacheObject<T>> get() {
        return cacheObjects.stream();
    }

    public List<T> getSourceList() {
        return cacheObjects.stream().map(CacheObject::getObject).collect(Collectors.toList());
    }

    @Override
    public Stream<CacheObject<T>> getSince(long timestamp) {
        return cacheObjects.stream().filter(cacheObject -> (cacheObject.getLastUpdated() > timestamp));
    }

    public List<?> getSourceListSince(long timestamp) {
        return cacheObjects
                .stream()
                .filter(cacheObject -> (cacheObject.getLastUpdated() >= timestamp))
                .map(CacheObject::getObject)
                .collect(Collectors.toList());
    }


    @SneakyThrows
    private void updateMetaData() {
        cacheMetaData.setCacheCount(cacheObjects.size());
        cacheMetaData.setLastUpdated(System.currentTimeMillis());
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        cacheObjects.stream().map(CacheObject::rawChecksum).forEach(digest::update);
        cacheMetaData.setChecksum(digest.digest());
    }

    private Map<String, CacheObject<T>> getMap(List<T> list) {
        return list.stream().map(CacheObject::new).collect(Collectors.toMap(CacheObject::getChecksum, Function.identity(), (a, b) -> b));
    }

    private void flushMetaData() {
        cacheMetaData.setCacheCount(0);
        cacheMetaData.setLastUpdated(0);
        cacheMetaData.setChecksum(null);
    }

    @Override
    public long getLastUpdated() {
        return cacheMetaData.getLastUpdated();
    }

    @Override
    public Stream<CacheObject<T>> filter(Predicate<T> predicate) {
        return cacheObjects.stream().filter(o -> predicate.test(o.getObject()));
    }
}
