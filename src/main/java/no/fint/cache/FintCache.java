package no.fint.cache;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.fint.cache.model.CacheMetaData;
import no.fint.cache.model.CacheObject;
import no.fint.cache.model.Index;
import no.fint.cache.model.SingleIndex;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
public class FintCache<T extends Serializable> implements Cache<T> {
    @Getter
    private CacheMetaData cacheMetaData;
    private List<CacheObject<T>> cacheObjectList;
    private Map<Integer, Index> index;

    public FintCache() {
        cacheMetaData = new CacheMetaData();
        cacheObjectList = Collections.emptyList();
    }

    @Override
    public void update(List<T> objects) {
        if (objects.isEmpty()) {
            log.debug("Empty list sent in, will not update cache");
        } else {
            Map<String, CacheObject<T>> cacheObjectMap = getMap(objects);
            updateInternal(cacheObjectMap);
        }
    }

    private void updateInternal(Map<String, CacheObject<T>> cacheObjectMap) {
        if (cacheObjectList.isEmpty()) {
            log.debug("Empty cache, adding all values");
            cacheObjectList = new ArrayList<>(cacheObjectMap.values());
        } else {
            List<CacheObject<T>> cacheObjectListCopy = new ArrayList<>(cacheObjectList);
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

    @Override
    public void updateCache(List<CacheObject<T>> objects) {
        if (objects.isEmpty()) {
            log.debug("Empty list sent in, will not update cache");
        } else {
            index = Collections.emptyMap();
            Map<String, CacheObject<T>> cacheObjectMap = getCacheMap(objects);
            updateInternal(cacheObjectMap);
        }
    }

    @Override
    public void add(List<T> objects) {
        Map<String, CacheObject<T>> newObjects = getMap(objects);
        addInternal(newObjects);
    }

    private void addInternal(Map<String, CacheObject<T>> newObjects) {
        List<CacheObject<T>> cacheObjectListCopy = new ArrayList<>(cacheObjectList);
        cacheObjectListCopy.addAll(newObjects.values());
        cacheObjectList = cacheObjectListCopy;
        updateMetaData();
    }

    @Override
    public void addCache(List<CacheObject<T>> objects) {
        Map<String, CacheObject<T>> newObjects = getCacheMap(objects);
        addInternal(newObjects);
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
        return cacheObjectList.stream().filter(cacheObject ->
                (cacheObject.getLastUpdated() >= timestamp))
                .collect(Collectors.toList())
                .stream().map(CacheObject::getObject)
                .collect(Collectors.toList());
    }


    @SneakyThrows
    private void updateMetaData() {
        Map<Integer, Index> newIndex = new HashMap<>();
        cacheMetaData.setCacheCount(cacheObjectList.size());
        cacheMetaData.setLastUpdated(System.currentTimeMillis());
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        ListIterator<CacheObject<T>> iterator = cacheObjectList.listIterator();
        while (iterator.hasNext()) {
            int i = iterator.nextIndex();
            CacheObject<T> it = iterator.next();
            digest.update(it.rawChecksum());
            IntStream.of(it.getHashCodes()).forEach(key -> newIndex.compute(key, (k,v) -> {
                if (v == null) {
                    return new SingleIndex(i);
                }
                return v.add(i);
            }));
        }
        cacheMetaData.setChecksum(digest.digest());
        index = newIndex;
    }

    private Map<String, CacheObject<T>> getMap(List<T> list) {
        return list.stream().map(CacheObject::new).collect(Collectors.toMap(CacheObject::getChecksum, Function.identity(), (a, b) -> b));
    }

    private Map<String, CacheObject<T>> getCacheMap(List<CacheObject<T>> list) {
        return list.stream().collect(Collectors.toMap(CacheObject::getChecksum, Function.identity(), (a, b) -> b));
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
        return cacheObjectList.parallelStream().filter(o -> predicate.test(o.getObject()));
    }

    @Override
    public Stream<CacheObject<T>> filter(int hashCode, Predicate<T> predicate) {
        if (!index.containsKey(hashCode)) {
            return filter(predicate);
        }
        return index.get(hashCode)
                .stream()
                .mapToObj(cacheObjectList::get)
                .filter(o -> predicate.test(o.getObject()));
    }
}
