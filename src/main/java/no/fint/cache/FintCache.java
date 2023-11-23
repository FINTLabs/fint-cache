package no.fint.cache;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.cache.model.*;

import java.io.Serializable;
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
    private FintCacheList<T> fintCacheList;
    private Map<Integer, Index> index;
    private NavigableMap<Long, BitSet> lastUpdatedIndex;

    public FintCache() {
        fintCacheList = new FintCacheList<>();
        flush();
    }

    @Override
    public void update(List<T> objects) {
        if (objects.isEmpty()) {
            log.debug("Empty list sent in, will not update cache");
        } else {
            Map<String, PojoCacheObject<T>> cacheObjectMap = getMap(objects);
            fintCacheList.updateInternal(cacheObjectMap);
            updateMetaData();
        }
    }

    @Override
    public void updateCache(List<CacheObject<T>> objects) {
        if (!objects.isEmpty()) {
            index = Collections.emptyMap();

            List<PojoCacheObject<T>> pojoCacheObjects = mapToPojoCacheObjects(objects);
            Map<String, PojoCacheObject<T>> cacheObjectMap = getCacheMap(pojoCacheObjects);
            fintCacheList.updateInternal(cacheObjectMap);
            updateMetaData();
        }
    }

    private List<PojoCacheObject<T>> mapToPojoCacheObjects(List<CacheObject<T>> cacheObjects) {
        return cacheObjects
                .stream()
                .map(PojoCacheObject::new)
                .collect(Collectors.toList());
    }

    @Override
    public void add(List<T> objects) {
        Map<String, PojoCacheObject<T>> newObjects = getMap(objects);
        fintCacheList.addInternal(newObjects);
        updateMetaData();
    }

    @Override
    public void addCache(List<CacheObject<T>> cacheObjects) {
        List<PojoCacheObject<T>> pojoCacheObjects = mapToPojoCacheObjects(cacheObjects);
        Map<String, PojoCacheObject<T>> cacheMap = getCacheMap(pojoCacheObjects);
        fintCacheList.addInternal(cacheMap);
        updateMetaData();
    }

    @Override
    public void flush() {
        cacheMetaData = new CacheMetaData();
        fintCacheList.flush();
        index = Collections.emptyMap();
        lastUpdatedIndex = Collections.emptyNavigableMap();
    }

    @Override
    public Stream<? extends CacheObjectType<T>> stream() {
        return fintCacheList.getCacheObjects().stream();
    }

    public List<T> getSourceList() {
        return fintCacheList.getCacheObjects().stream().map(CacheObjectType::getObject).collect(Collectors.toList());
    }

    @Override
    public Stream<? extends CacheObjectType<T>> streamSince(long timestamp) {
        return lastUpdatedIndex
                .tailMap(timestamp, false)
                .values()
                .stream()
                .flatMapToInt(BitSet::stream)
                .mapToObj(index -> fintCacheList.getCacheObjects().get(index));
    }

    public List<?> getSourceListSince(long timestamp) {
        return streamSince(timestamp).map(CacheObjectType::getObject).collect(Collectors.toList());
    }

    private void updateMetaData() {
        Map<Integer, Index> newIndex = new HashMap<>();
        NavigableMap<Long, BitSet> newLastUpdatedIndex = new TreeMap<>();
        cacheMetaData.setCacheCount(fintCacheList.getCacheObjects().size());
        cacheMetaData.setLastUpdated(System.currentTimeMillis());
        ListIterator<? extends CacheObjectType<T>> iterator = fintCacheList.getCacheObjects().listIterator();
        while (iterator.hasNext()) {
            int i = iterator.nextIndex();
            CacheObjectType<T> it = iterator.next();
            IntStream.of(it.getHashCodes()).forEach(key -> newIndex.compute(key, createIndex(i)));
            newLastUpdatedIndex.computeIfAbsent(it.getLastUpdated(), k -> new BitSet()).set(i);
        }
        cacheMetaData.setSize(fintCacheList.getCacheObjects().parallelStream().mapToLong(CacheObjectType::getSize).sum());
        index = newIndex;
        lastUpdatedIndex = newLastUpdatedIndex;
    }

    private static <K> BiFunction<K, Index, Index> createIndex(int i) {
        return (k, v) -> {
            if (v == null) {
                return new SingleIndex(i);
            }
            return v.add(i);
        };
    }

    private Map<String, PojoCacheObject<T>> getMap(List<T> list) {
        return list.parallelStream().map(PojoCacheObject::new).collect(Collectors.toMap(PojoCacheObject::getChecksum, Function.identity(), (a, b) -> b));
    }

    private Map<String, PojoCacheObject<T>> getCacheMap(List<PojoCacheObject<T>> list) {
        return list.parallelStream().collect(Collectors.toMap(PojoCacheObject::getChecksum, Function.identity(), (a, b) -> b));
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
    public Stream<? extends CacheObjectType<T>> filter(Predicate<T> predicate) {
        return fintCacheList.getCacheObjects().stream().filter(o -> predicate.test(o.getObject()));
    }

    @Override
    public Stream<? extends CacheObjectType<T>> filter(int hashCode, Predicate<T> predicate) {
        if (!index.containsKey(hashCode)) {
            return filter(predicate);
        }
        return index.get(hashCode)
                .stream()
                .mapToObj(index -> fintCacheList.getCacheObjects().get(index))
                .filter(o -> predicate.test(o.getObject()));
    }
}