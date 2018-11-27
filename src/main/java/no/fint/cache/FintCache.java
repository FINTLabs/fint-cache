package no.fint.cache;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.fint.cache.model.CacheMetaData;
import no.fint.cache.model.CacheObject;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class FintCache<T extends Serializable> implements Cache<T>, Serializable {
    @Getter
    private CacheMetaData cacheMetaData;
    private Set<CacheObject<T>> cacheObjectList;

    public FintCache() {
        cacheMetaData = new CacheMetaData();
        cacheObjectList = new HashSet<>();
    }

    @Override
    public void update(List<T> objects) {
        if (objects.isEmpty()) {
            log.debug("Empty list sent in, will not update cache");
        } else {
            Map<String, CacheObject<T>> cacheObjectMap = getMap(objects);
            if (cacheObjectList.isEmpty()) {
                log.debug("Empty cache, adding all values");
                cacheObjectList.addAll(cacheObjectMap.values());
            } else {
                Set<CacheObject<T>> cacheObjectListCopy = new HashSet<>(cacheObjectList);
                cacheObjectList.forEach(cacheObject -> {
                    String checksum = cacheObject.getChecksum();
                    if (cacheObjectMap.containsKey(checksum)) {
                        cacheObjectMap.remove(checksum);
                    } else {
                        log.debug("Adding new object to the cache (checksum: {})", cacheObject.getChecksum());
                        cacheObjectListCopy.remove(cacheObject);
                    }
                });

                cacheObjectListCopy.addAll(cacheObjectMap.values());
                cacheObjectList = cacheObjectListCopy;
            }

            updateMetaData();
        }
    }

    @Override
    public void add(List<T> objects) {
        Map<String, CacheObject<T>> newObjects = getMap(objects);
        Set<CacheObject<T>> cachObjectListCopy = new HashSet<>(cacheObjectList);
        cachObjectListCopy.addAll(newObjects.values());
        cacheObjectList = cachObjectListCopy;
        updateMetaData();
    }


    @Override
    public void flush() {
        flushMetaData();
        cacheObjectList.clear();
    }

    @Override
    public Stream<CacheObject<T>> get() {
        return cacheObjectList.stream();
    }

    public List<T> getSourceList() {
        return cacheObjectList.stream().map(CacheObject::getObject).collect(Collectors.toList());
    }

    @Override
    public Stream<CacheObject<T>> getSince(long timestamp) {
        return cacheObjectList.stream().filter(cacheObject -> (cacheObject.getLastUpdated() > timestamp));
    }

    public List<?> getSourceListSince(long timestamp) {
        return cacheObjectList
                .stream()
                .filter(cacheObject -> (cacheObject.getLastUpdated() >= timestamp))
                .map(CacheObject::getObject)
                .collect(Collectors.toList());
    }


    @SneakyThrows
    private void updateMetaData() {
        cacheMetaData.setCacheCount(cacheObjectList.size());
        cacheMetaData.setLastUpdated(System.currentTimeMillis());
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        cacheObjectList.stream().map(CacheObject::rawChecksum).forEach(digest::update);
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
        return cacheObjectList.stream().filter(o -> predicate.test(o.getObject()));
    }
}
