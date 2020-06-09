package no.fint.cache;

import com.google.common.collect.ImmutableList;
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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
public class FintCache<T extends Serializable> implements Cache<T>, Serializable {
    @Getter
    private CacheMetaData cacheMetaData;
    private List<CacheObject<T>> cacheObjects;
    private Map<Integer, Index> index;
    private NavigableMap<Long, Index> timestampIndex;

    public FintCache() {
        cacheMetaData = new CacheMetaData();
        cacheObjects = Collections.emptyList();
        index = Collections.emptyMap();
        timestampIndex = Collections.emptyNavigableMap();
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
        if (cacheObjects.isEmpty()) {
            log.debug("Empty cache, adding all values");
            cacheObjects = ImmutableList.copyOf(cacheObjectMap.values());
        } else {
            List<CacheObject<T>> cacheObjectsCopy = new ArrayList<>(cacheObjects);
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
            cacheObjects = ImmutableList.sortedCopyOf(Comparator.comparing(CacheObject::getChecksum), cacheObjectsCopy);
        }

        updateMetaData();
    }

    @Override
    public void updateCache(List<CacheObject<T>> objects) {
        if (!objects.isEmpty()) {
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
        List<CacheObject<T>> cacheObjectsCopy = new ArrayList<>(cacheObjects);
        cacheObjectsCopy.addAll(newObjects.values());
        cacheObjects = ImmutableList.sortedCopyOf(Comparator.comparing(CacheObject::getChecksum), cacheObjectsCopy);
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
        cacheObjects = Collections.emptyList();
    }

    @Override
    public Stream<CacheObject<T>> stream() {
        return cacheObjects.stream();
    }

    public List<T> getSourceList() {
        return cacheObjects.stream().map(CacheObject::getObject).collect(Collectors.toList());
    }

    @Override
    public Stream<CacheObject<T>> streamSince(long timestamp) {
        return timestampIndex
                .tailMap(timestamp, false)
                .values()
                .stream()
                .flatMapToInt(Index::stream)
                .mapToObj(cacheObjects::get);
        //return cacheObjects.stream().filter(cacheObject -> (cacheObject.getLastUpdated() > timestamp));
    }

    public List<?> getSourceListSince(long timestamp) {
        return streamSince(timestamp).map(CacheObject::getObject).collect(Collectors.toList());
        /*return cacheObjects
                .stream()
                .filter(cacheObject -> (cacheObject.getLastUpdated() >= timestamp))
                .map(CacheObject::getObject)
                .collect(Collectors.toList());

         */
    }


    @SneakyThrows
    private void updateMetaData() {
        Map<Integer, Index> newIndex = new HashMap<>();
        NavigableMap<Long, Index> newTimestampIndex = new TreeMap<>();
        cacheMetaData.setCacheCount(cacheObjects.size());
        cacheMetaData.setLastUpdated(System.currentTimeMillis());
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        ListIterator<CacheObject<T>> iterator = cacheObjects.listIterator();
        while (iterator.hasNext()) {
            int i = iterator.nextIndex();
            CacheObject<T> it = iterator.next();
            digest.update(it.rawChecksum());
            IntStream.of(it.getHashCodes()).forEach(key -> newIndex.compute(key, createIndex(i)));
            newTimestampIndex.compute(it.getLastUpdated(), createIndex(i));
        }
        cacheMetaData.setChecksum(digest.digest());
        cacheMetaData.setSize(cacheObjects.parallelStream().mapToLong(CacheObject::getSize).sum());
        index = newIndex;
        timestampIndex = newTimestampIndex;
    }

    private static <K> BiFunction<K, Index, Index> createIndex(int i) {
        return (k, v) -> {
            if (v == null) {
                return new SingleIndex(i);
            }
            return v.add(i);
        };
    }

    private Map<String, CacheObject<T>> getMap(List<T> list) {
        return list.parallelStream().map(CacheObject::new).collect(Collectors.toMap(CacheObject::getChecksum, Function.identity(), (a, b) -> b));
    }

    private Map<String, CacheObject<T>> getCacheMap(List<CacheObject<T>> list) {
        return list.parallelStream().collect(Collectors.toMap(CacheObject::getChecksum, Function.identity(), (a, b) -> b));
    }

    private void flushMetaData() {
        cacheMetaData.setCacheCount(0);
        cacheMetaData.setLastUpdated(0);
        cacheMetaData.setChecksum(null);
        cacheMetaData.setSize(0L);
    }

    @Override
    public long getLastUpdated() {
        return cacheMetaData.getLastUpdated();
    }

    @Override
    public int size() {
        return cacheMetaData.getCacheCount();
    }

    @Override
    public long volume() {
        return cacheMetaData.getSize();
    }

    @Override
    public Stream<CacheObject<T>> filter(Predicate<T> predicate) {
        return cacheObjects.stream().filter(o -> predicate.test(o.getObject()));
    }

    @Override
    public Stream<CacheObject<T>> filter(int hashCode, Predicate<T> predicate) {
        if (!index.containsKey(hashCode)) {
            return filter(predicate);
        }
        return index.get(hashCode)
                .stream()
                .mapToObj(cacheObjects::get)
                .filter(o -> predicate.test(o.getObject()));
    }
}
