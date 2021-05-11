package no.fint.cache;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.fint.cache.model.CacheMetaData;
import no.fint.cache.model.CacheObject;
import no.fint.cache.model.Index;
import no.fint.cache.model.SingleIndex;
import org.apache.commons.codec.binary.Hex;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.SerializationUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static no.fint.cache.FintCache.getCacheMap;
import static no.fint.cache.FintCache.getMap;

@Slf4j
public class RedisCache<T extends Serializable> implements Cache<T> {

    @Data
    static class MetaObject implements Serializable {
        private long lastUpdated;
        private int[] hashCodes;
        private int size;
    }

    private final String prefix;
    private final ValueOperations<String, byte[]> valueOperations;

    private CacheMetaData cacheMetaData;
    private List<String> cacheKeys;
    private ImmutableMap<Integer, Index> index;
    private ImmutableMultimap<Long, String> lastUpdatedMultimap;

    public RedisCache(String prefix, RedisTemplate<String, byte[]> redisTemplate) {
        this.prefix = prefix;
        this.valueOperations = redisTemplate.opsForValue();
        cacheMetaData = new CacheMetaData();
        cacheKeys = Collections.emptyList();
        index = ImmutableMap.of();
        lastUpdatedMultimap = ImmutableMultimap.of();
    }

    @Override
    public void update(List<T> objects) {
        log.info("Update {}", objects.size());
        if (objects.isEmpty()) {
            log.debug("Empty list sent in, will not update cache");
        } else {
            Map<String, CacheObject<T>> cacheObjectMap = getMap(objects);
            updateInternal(cacheObjectMap);
        }
    }

    @Override
    public void updateCache(List<CacheObject<T>> objects) {
        log.info("Update {}", objects.size());
        if (!objects.isEmpty()) {
            index = ImmutableMap.of();
            Map<String, CacheObject<T>> cacheObjectMap = getCacheMap(objects);
            updateInternal(cacheObjectMap);
        }
    }

    @Override
    public void add(List<T> objects) {
        log.info("Add {}", objects.size());
        Map<String, CacheObject<T>> newObjects = getMap(objects);
        addInternal(newObjects);
    }

    @Override
    public void addCache(List<CacheObject<T>> objects) {
        log.info("Add {}", objects.size());
        Map<String, CacheObject<T>> newObjects = getCacheMap(objects);
        addInternal(newObjects);
    }

    private void addInternal(Map<String, CacheObject<T>> cacheObjectMap) {
        final List<String> newCacheKeys = new LinkedList<>(cacheKeys);

        long size = cacheMetaData.getSize();
        final HashMap<Integer, Index> newIndex = new HashMap<>(index);
        final ImmutableMultimap.Builder<Long, String> lastUpdatedBuilder = ImmutableMultimap.<Long, String>builder().putAll(lastUpdatedMultimap);

        for (Map.Entry<String, CacheObject<T>> entry : cacheObjectMap.entrySet()) {
            String checksum = entry.getKey();
            CacheObject<T> it = entry.getValue();
            insertEntry(checksum, it);
            newCacheKeys.add(checksum);
            int i = newCacheKeys.size() - 1;
            updateIndex(newIndex, it, i);
            size += it.getSize();
            lastUpdatedBuilder.put(it.getLastUpdated(), it.getChecksum());

        }

        cacheMetaData.setCacheCount(cacheKeys.size());
        cacheMetaData.setLastUpdated(System.currentTimeMillis());
        cacheMetaData.setSize(size);
        cacheKeys = ImmutableList.copyOf(newCacheKeys);
        lastUpdatedMultimap = lastUpdatedBuilder.build();
        index = ImmutableMap.copyOf(newIndex);
    }


    private void updateInternal(Map<String, CacheObject<T>> cacheObjectMap) {
        final ImmutableMultimap<String, Long> lastUpdatedByChecksum = lastUpdatedMultimap.inverse();

        final List<String> removed = lastUpdatedByChecksum.keys().stream().filter(k -> !cacheObjectMap.containsKey(k)).collect(Collectors.toList());
        log.info("Removing {}", removed.size());
        valueOperations.getOperations().delete(removed.stream().map(this::dataKey).collect(Collectors.toList()));
        valueOperations.getOperations().delete(removed.stream().map(this::metaKey).collect(Collectors.toList()));

        cacheObjectMap
                .keySet()
                .stream()
                .filter(lastUpdatedByChecksum::containsKey)
                .forEach(k -> {
                    valueOperations.getOperations().expire(dataKey(k), 30, TimeUnit.MINUTES);
                    valueOperations.getOperations().expire(metaKey(k), 30, TimeUnit.MINUTES);
                });

        cacheObjectMap
                .entrySet()
                .stream()
                .filter(e -> !lastUpdatedByChecksum.containsKey(e.getKey()))
                .forEach(e -> insertEntry(e.getKey(), e.getValue()));
        cacheKeys = ImmutableList.sortedCopyOf(cacheObjectMap.keySet());
        updateMetaData(cacheObjectMap);
    }

    private void updateMetaData(Map<String, CacheObject<T>> cacheObjectMap) {
        Map<Integer, Index> newIndex = new HashMap<>();
        cacheMetaData.setCacheCount(cacheKeys.size());
        cacheMetaData.setLastUpdated(System.currentTimeMillis());
        ListIterator<String> iterator = cacheKeys.listIterator();
        final ImmutableMultimap.Builder<Long, String> lastUpdatedBuilder = ImmutableMultimap.builder();
        long size = 0;
        while (iterator.hasNext()) {
            int i = iterator.nextIndex();
            CacheObject<T> it = cacheObjectMap.get(iterator.next());
            updateIndex(newIndex, it, i);
            size += it.getSize();
            lastUpdatedBuilder.put(it.getLastUpdated(), it.getChecksum());
        }
        cacheMetaData.setSize(size);
        index = ImmutableMap.copyOf(newIndex);
        lastUpdatedMultimap = lastUpdatedBuilder.build();
    }

    private void insertEntry(String key, CacheObject<T> value) {
        valueOperations.set(dataKey(key), value.getBytes(), 30, TimeUnit.MINUTES);
        MetaObject m = new MetaObject();
        m.setHashCodes(value.getHashCodes());
        m.setSize(value.getSize());
        m.setLastUpdated(value.getLastUpdated());
        valueOperations.set(metaKey(key), SerializationUtils.serialize(m), 30, TimeUnit.MINUTES);
    }

    private void updateIndex(Map<Integer, Index> newIndex, CacheObject<T> it, int i) {
        IntStream.of(it.getHashCodes()).forEach(key -> newIndex.compute(key, (k, v) -> {
            if (v == null) {
                return new SingleIndex(i);
            }
            return v.add(i);
        }));
    }

    @SneakyThrows
    private CacheObject<T> getCacheObject(String key) {
        MetaObject metaObject = (MetaObject) SerializationUtils.deserialize(valueOperations.get(metaKey(key)));
        if (metaObject == null) {
            return null;
        }
        return new CacheObject<>(() -> valueOperations.get(dataKey(key)),
                metaObject.getLastUpdated(),
                metaObject.getSize(),
                metaObject.getHashCodes(),
                Hex.decodeHex(key));
    }

    private String dataKey(String key) {
        return prefix + ":data:" + key;
    }

    private String metaKey(String key) {
        return prefix + ":meta:" + key;
    }

    @Override
    public void flush() {
        cacheMetaData = new CacheMetaData();
        cacheKeys = Collections.emptyList();
        index = ImmutableMap.of();
        lastUpdatedMultimap = ImmutableMultimap.of();
    }

    @Override
    public Stream<CacheObject<T>> stream() {
        return cacheKeys.stream().map(this::getCacheObject);
    }

    @Override
    public Stream<CacheObject<T>> streamSince(long timestamp) {
        return lastUpdatedMultimap.entries().stream().filter(e -> e.getKey() > timestamp).map(Map.Entry::getValue).map(this::getCacheObject);
    }

    @Override
    public Stream<CacheObject<T>> filter(Predicate<T> predicate) {
        return stream().filter(o -> predicate.test(o.getObject()));
    }

    @Override
    public Stream<CacheObject<T>> filter(int hashCode, Predicate<T> predicate) {
        if (!index.containsKey(hashCode))
            return filter(predicate);
        return index.get(hashCode)
                .stream()
                .mapToObj(cacheKeys::get)
                .map(this::getCacheObject)
                .filter(o -> predicate.test(o.getObject()));

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

}
