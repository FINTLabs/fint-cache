package no.fint.cache;

import com.hazelcast.core.IList;
import lombok.extern.slf4j.Slf4j;
import no.fint.cache.model.CacheObject;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class HazelcastCache<T extends Serializable> implements Cache<T> {

    private final IList<CacheObject<T>> datastore;

    public HazelcastCache(IList<CacheObject<T>> datastore) {
        this.datastore = datastore;
    }

    @Override
    public void update(List<T> objects) {
        if (objects.isEmpty()) {
            log.debug("Empty list sent in, will not update cache");
            return;
        }
        datastore.clear();
        objects.stream().map(CacheObject::new).forEach(datastore::add);
    }

    @Override
    public void add(List<T> objects) {
        Map<String, CacheObject<T>> newItems = getMap(objects);
        for (int i = 0; i < datastore.size(); ++i) {
            if (newItems.containsKey(datastore.get(i).getChecksum())) {
                datastore.remove(i);
            }
        }
        datastore.addAll(newItems.values());
    }

    @Override
    public void flush() {
        datastore.clear();
    }

    @Override
    public Stream<CacheObject<T>> get() {
        return datastore.stream();
    }

    @Override
    public Stream<CacheObject<T>> getSince(long timestamp) {
        return datastore.stream().filter(i -> i.getLastUpdated() >= timestamp);
    }

    @Override
    public long getLastUpdated() {
        return datastore.stream().mapToLong(CacheObject::getLastUpdated).max().orElse(0L);
    }

    @Override
    public Stream<CacheObject<T>> filter(Predicate<T> predicate) {
        return datastore.stream().filter(i -> predicate.test(i.getObject()));
    }

    private Map<String, CacheObject<T>> getMap(List<T> items) {
        return items.stream().map(CacheObject::new).collect(Collectors.toMap(CacheObject::getChecksum, Function.identity()));
    }
}
