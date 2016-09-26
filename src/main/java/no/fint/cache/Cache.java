package no.fint.cache;

import no.fint.cache.model.CacheObject;

import java.util.List;
import java.util.Map;

public interface Cache {
    void update(Map<String, CacheObject> cacheObjectMap);
    void refresh(Map<String, CacheObject> cacheObjectMap);
    void flush();
    List<CacheObject> get();
    List<CacheObject> getSince(long timestamp);
}
