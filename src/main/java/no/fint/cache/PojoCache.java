package no.fint.cache;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.cache.model.CacheMetaData;
import no.fint.cache.model.CacheObject;
import no.fint.cache.model.PojoCacheObject;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class PojoCache<T extends Serializable> implements Cache<T>, Serializable {

    @Getter
    private CacheMetaData cacheMetaData;
    private List<PojoCacheObject<T>> pojoCacheObjects;

    public PojoCache() {
        cacheMetaData = new CacheMetaData();
        cacheMetaData.setSize(-1);
    }

    @Override
    public void update(List<T> objects) {
        if (objects.isEmpty()) {
            log.debug("Empty list sent in, will not update cache");
        } else {
            objects.forEach(object -> {
                if (!contains(object)) {
                    pojoCacheObjects.add(new PojoCacheObject<>(object));
                    log.debug("Adding new object to the cache");
                }
            });
        }
    }

    @Override
    public void updateCache(List<CacheObject<T>> cacheObjects) {
        cacheObjects.forEach(object -> {
            if (contains(object.getObject())) {
                if (object instanceof PojoCacheObject) {
                    pojoCacheObjects.add((PojoCacheObject<T>) object);
                } else {
                    pojoCacheObjects.add(new PojoCacheObject<>(object.getObject()));
                }
            }
        });
    }

    private boolean contains(T element) {
        return pojoCacheObjects
                .stream()
                .anyMatch(pojoCacheObject -> pojoCacheObject.getObject().equals(element));
    }

    @Override
    public void add(List<T> objects) {
        pojoCacheObjects.addAll(convertToCacheObject(objects));
        updateMetaData();
    }

    @Override
    public void addCache(List<CacheObject<T>> cacheObjects) {
        cacheObjects.forEach(object -> {
            if (object instanceof PojoCacheObject) {
                pojoCacheObjects.add((PojoCacheObject<T>) object);
            } else {
                pojoCacheObjects.add(new PojoCacheObject<>(object.getObject()));
            }
        });
        updateMetaData();
    }

    @Override
    public void flush() {
        cacheMetaData = new CacheMetaData();
        cacheMetaData.setSize(-1);
        pojoCacheObjects.clear();
    }

    @Override
    public Stream<CacheObject<T>> stream() {
        return null;
    }

    @Override
    public Stream<CacheObject<T>> streamSince(long timestamp) {
        return null;
    }

    @Override
    public long getLastUpdated() {
        return cacheMetaData.getLastUpdated();
    }

    @Override
    public int size() {
        return pojoCacheObjects.size();
    }

    @Override
    public long volume() {
        return -1;
    }

    @Override
    public Stream<CacheObject<T>> filter(Predicate<T> predicate) {
        return null;
    }

    @Override
    public Stream<CacheObject<T>> filter(int hashCode, Predicate<T> predicate) {
        return null;
    }

    private void updateMetaData() {
        cacheMetaData.setCacheCount(pojoCacheObjects.size());
        cacheMetaData.setLastUpdated(System.currentTimeMillis());
    }

    private List<PojoCacheObject<T>> convertToCacheObject(List<T> list) {
        return list
                .stream()
                .map(PojoCacheObject::new)
                .collect(Collectors.toList());
    }

}
