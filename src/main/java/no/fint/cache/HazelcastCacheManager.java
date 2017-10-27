package no.fint.cache;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.ISet;
import lombok.extern.slf4j.Slf4j;
import no.fint.cache.model.CacheObject;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class HazelcastCacheManager<T extends Serializable> implements CacheManager<T> {

    @Autowired
    private HazelcastInstance hazelcast;

    private ISet<String> caches;

    @Override
    public Optional<Cache<T>> getCache(String key) {
        if (caches.contains(key)) {
            return Optional.of(new HazelcastCache<T>(hazelcast.getList(key)));
        }
        return Optional.empty();
    }

    @Override
    public Cache<T> createCache(String key) {
        IList<CacheObject<T>> cacheObjects = hazelcast.getList(key);
        log.info("Created cache {} on service {}", cacheObjects.getName(), cacheObjects.getServiceName());
        caches.add(key);
        return new HazelcastCache<>(cacheObjects);
    }

    @Override
    public void remove(String key) {
        hazelcast.getList(key).destroy();
        caches.remove(key);
    }

    @Override
    public boolean hasItems() {
        return !caches.isEmpty();
    }

    @Override
    public Set<String> getKeys() {
        return caches;
    }

    @PostConstruct
    public void init() {
        caches = hazelcast.getSet("fint-caches");
        log.info("Connected to Hazelcast {} with service name {}", hazelcast.getName(), caches.getServiceName());
    }
}
