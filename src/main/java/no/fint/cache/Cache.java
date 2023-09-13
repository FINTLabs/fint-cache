package no.fint.cache;

import no.fint.cache.model.CacheObjectType;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Cache<T extends Serializable> {
    void update(List<T> objects);

    void updateCache(List<? extends CacheObjectType<T>> objects);

    void add(List<T> objects);

    void addCache(List<? extends CacheObjectType<T>> objects);

    void flush();

    Stream<? extends CacheObjectType<T>> stream();

    Stream<? extends CacheObjectType<T>> streamSince(long timestamp);

    long getLastUpdated();

    int size();

    long volume();

    Stream<? extends CacheObjectType<T>> filter(Predicate<T> predicate);

    Stream<? extends CacheObjectType<T>> filter(int hashCode, Predicate<T> predicate);
}
