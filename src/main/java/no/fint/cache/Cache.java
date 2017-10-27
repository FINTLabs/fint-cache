package no.fint.cache;

import no.fint.cache.model.CacheObject;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Cache<T extends Serializable> {
    void update(List<T> objects);

    void add(List<T> objects);

    void flush();

    Stream<CacheObject<T>> get();

    Stream<CacheObject<T>> getSince(long timestamp);

    long getLastUpdated();

    Stream<CacheObject<T>> filter(Predicate<T> predicate);
}
