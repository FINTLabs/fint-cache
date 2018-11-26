package no.fint.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
        return getCache(orgId).map(Cache::getLastUpdated).orElse(0L);
    }

    public Optional<Cache<T>> getCache(String orgId) {
        return cacheManager.getCache(CacheUri.create(orgId, model));
    }

    public List<T> getAll(String orgId) {
        Optional<Cache<T>> cache = getCache(orgId);
        if (cache.isPresent()) {
            return cache.get().get().map(CacheObject::getObject).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public List<T> getAll(String orgId, long sinceTimestamp) {
        Optional<Cache<T>> cache = getCache(orgId);
        if (cache.isPresent()) {
            return cache.get().getSince(sinceTimestamp).map(CacheObject::getObject).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public Optional<T> getOne(String orgId, Predicate<T> idFunction) {
        Optional<Cache<T>> cache = getCache(orgId);
        if (cache.isPresent()) {
            return cache.get().filter(idFunction).max(Comparator.comparingLong(CacheObject::getLastUpdated)).map(CacheObject::getObject);
        }
        return Optional.empty();
    }

    public Optional<T> getOne(String orgId, int hashCode, Predicate<T> idFunction) {
        Optional<Cache<T>> cache = getCache(orgId);
        if (cache.isPresent()) {
            return cache.get().filter(hashCode, idFunction).max(Comparator.comparingLong(CacheObject::getLastUpdated)).map(CacheObject::getObject);
        }
        return Optional.empty();
    }

    public void update(String orgId, List<T> objects) {
        Optional<Cache<T>> cache = getCache(orgId);
        cache.ifPresent(c -> c.update(objects));
    }

    @Deprecated
    public void update(Event event, TypeReference<List<T>> typeReference) {
    	log.info("Updating cache for org {} for type {}", event.getOrgId(), typeReference.getType());
        List<T> objects = objectMapper.convertValue(event.getData(), typeReference);
        update(event.getOrgId(), objects);
    }

    public void add(String orgId, List<T> objects) {
        Optional<Cache<T>> cache = getCache(orgId);
        cache.ifPresent(c -> c.add(objects));
    }

    public void flush(String orgId) {
        Optional<Cache<T>> cache = getCache(orgId);
        cache.ifPresent(Cache::flush);
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
}
