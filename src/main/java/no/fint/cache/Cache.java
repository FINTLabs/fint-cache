package no.fint.cache;

import no.fint.cache.model.CacheObject;

import java.util.List;

public interface Cache {
    void update(List<?> objects);
    void refresh(List<?> objects);
    void flush();
    List<CacheObject<?>> get();
    List<CacheObject<?>> getSince(long timestamp);
}
