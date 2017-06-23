package no.fint.cache;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.cache.model.CacheMetaData;
import no.fint.cache.model.CacheObject;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class FintCache<T> implements Cache<T> {
    @Getter
    private CacheMetaData cacheMetaData;
    private Set<CacheObject<T>> cacheObjectList;

    public FintCache() {
        cacheMetaData = new CacheMetaData();
        cacheObjectList = new HashSet<>();
    }

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

    public void add(List<T> objects) {
        Map<String, CacheObject<T>> newObjects = getMap(objects);
        Set<CacheObject<T>> cachObjectListCopy = new HashSet<>(cacheObjectList);
        cachObjectListCopy.addAll(newObjects.values());
        cacheObjectList = cachObjectListCopy;
        updateMetaData();
    }

    public void refresh(List<T> objects) {
        cacheObjectList.clear();
        update(objects);
    }

    public void flush() {
        flushMetaData();
        cacheObjectList.clear();
    }

    public List<CacheObject<T>> get() {
        return new ArrayList<>(cacheObjectList);
    }

    public List<T> getSourceList() {
        return cacheObjectList.stream().map(CacheObject::getObject).collect(Collectors.toList());
    }

    public List<CacheObject<T>> getSince(long timestamp) {
        Set<CacheObject<T>> values = cacheObjectList.stream().filter(cacheObject ->
                (cacheObject.getLastUpdated() >= timestamp)).collect(Collectors.toSet());
        return new ArrayList<>(values);
    }

    public List<?> getSourceListSince(long timestamp) {
        return cacheObjectList.stream().filter(cacheObject ->
                (cacheObject.getLastUpdated() >= timestamp))
                .collect(Collectors.toList())
                .stream().map(CacheObject::getObject)
                .collect(Collectors.toList());
    }


    private void updateMetaData() {
        cacheMetaData.setCacheCount(cacheObjectList.size());
        cacheMetaData.setLastUpdated(System.currentTimeMillis());
        cacheMetaData.setChecksum(DigestUtils.sha1Hex(cacheObjectList.toString().getBytes()));
    }

    private Map<String, CacheObject<T>> getMap(List<T> list) {
        Map<String, CacheObject<T>> cacheObjectMap = new HashMap<>();
        list.forEach(o -> {
            CacheObject<T> cacheObject = new CacheObject<>(o);
            cacheObjectMap.put(cacheObject.getChecksum(), cacheObject);
        });

        return cacheObjectMap;
    }

    private void flushMetaData() {
        cacheMetaData.setCacheCount(0);
        cacheMetaData.setLastUpdated(0);
        cacheMetaData.setChecksum(null);
    }

    public long getLastUpdated() {
        return cacheMetaData.getLastUpdated();
    }
}
