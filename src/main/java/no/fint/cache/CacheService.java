package no.fint.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.cache.exceptions.CacheNotFoundException;
import no.fint.cache.model.CacheObject;
import no.fint.cache.utils.CacheUri;
import no.fint.event.model.Event;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public abstract class CacheService<T extends Serializable> {

    @Getter
    private final String model;

    @Getter
    private List<Enum> actions;

    @Autowired(required = false)
    @Deprecated
    private ObjectMapper objectMapper;

    @Autowired
    private CacheManager<T> cacheManager;

    public boolean hasItems() {
        return cacheManager.hasItems();
    }

    public Set<String> getKeys() {
        return cacheManager.getKeys();
    }

    public CacheService(String model, Enum firstAction, Enum... actions) {
        this.model = model;
        this.actions = new ArrayList<>();
        this.actions.add(firstAction);
        this.actions.addAll(Arrays.asList(actions));
    }

    @PostConstruct
    public void init() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        log.info("Cache Manager {}", cacheManager);
    }

    public Cache<T> createCache(String orgId) {
        return cacheManager.createCache(CacheUri.create(orgId, model));
    }

    @Deprecated
    public void put(String orgId, Cache<T> cache) {
        createCache(orgId);
    }

    public long getLastUpdated(String orgId) {
        return getCache(orgId).map(Cache::getLastUpdated).orElseThrow(() -> new CacheNotFoundException(orgId));
    }

    public int getCacheSize(String orgId) {
        return getCache(orgId).map(Cache::size).orElseThrow(() -> new CacheNotFoundException(orgId));
    }

    public Optional<Cache<T>> getCache(String orgId) {
        return cacheManager.getCache(CacheUri.create(orgId, model));
    }

    public List<T> getAll(String orgId) {
        Cache<T> cache = getCache(orgId).orElseThrow(() -> new CacheNotFoundException(orgId));
        return cache.get().map(CacheObject::getObject).collect(Collectors.toList());
    }

    public List<T> getAll(String orgId, long sinceTimestamp) {
        Cache<T> cache = getCache(orgId).orElseThrow(() -> new CacheNotFoundException(orgId));
        return cache.getSince(sinceTimestamp).map(CacheObject::getObject).collect(Collectors.toList());
    }

    public Optional<T> getOne(String orgId, Predicate<T> idFunction) {
        Cache<T> cache = getCache(orgId).orElseThrow(() -> new CacheNotFoundException(orgId));
        return cache.filter(idFunction).max(Comparator.comparingLong(CacheObject::getLastUpdated)).map(CacheObject::getObject);
    }

    public Optional<T> getOne(String orgId, int hashCode, Predicate<T> idFunction) {
        Cache<T> cache = getCache(orgId).orElseThrow(() -> new CacheNotFoundException(orgId));
        return cache.filter(hashCode, idFunction).max(Comparator.comparingLong(CacheObject::getLastUpdated)).map(CacheObject::getObject);
    }

    public void update(String orgId, List<T> objects) {
        getCache(orgId).ifPresent(c -> c.update(objects));
    }

    public void updateCache(String orgId, List<CacheObject<T>> objects) {
        Optional<Cache<T>> cache = getCache(orgId);
        cache.ifPresent(c -> c.updateCache(objects));
    }

    @Deprecated
    public void update(Event event, TypeReference<List<T>> typeReference) {
        log.warn("Deprecated method call", new Exception());
        log.info("Updating cache for org {} for type {}", event.getOrgId(), typeReference.getType());
        List<T> objects = objectMapper.convertValue(event.getData(), typeReference);
        update(event.getOrgId(), objects);
    }

    public void add(String orgId, List<T> objects) {
        getCache(orgId).ifPresent(c -> c.add(objects));
    }

    public void addCache(String orgId, List<CacheObject<T>> objects) {
        Optional<Cache<T>> cache = getCache(orgId);
        cache.ifPresent(c -> c.addCache(objects));
    }

    public void flush(String orgId) {
        getCache(orgId).ifPresent(Cache::flush);
    }

    public void remove(String orgId) {
        cacheManager.remove(CacheUri.create(orgId, model));
    }

    public boolean supportsAction(Enum action) {
        return actions.contains(action);
    }

    public boolean supportsAction(String action) {
        return actions.stream().map(Enum::name).anyMatch(action::equals);
    }

    public abstract void onAction(Event event);

    public abstract void populateCache(String orgId);
}
