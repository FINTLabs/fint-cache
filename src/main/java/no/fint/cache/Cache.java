package no.fint.cache;

import no.fint.cache.model.CacheObject;

import java.util.List;

public interface Cache<T> {
    void update(List<T> objects);

    void add(List<T> objects);

    void refresh(List<T> objects);

    void flush();

    List<CacheObject<T>> get();

    List<CacheObject<T>> getSince(long timestamp);
}
