package no.fint.cache.service;

import no.fint.cache.model.Cache;
import no.fint.cache.model.CacheObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CacheService {

    private Cache cache;

    public CacheService() {
        cache = new Cache();
    }

    public void flush() {
        cache.flush();
    }

    public void update(List<Object> objects) {

        cache.update(getMap(objects));
    }

    public void refresh(List<Object> objects) {
        cache.refresh(getMap(objects));
    }

    private Map<String, CacheObject> getMap(List<Object> objects) {
        Map<String, CacheObject> cacheObjectMap = new HashMap<>();
        objects.stream().forEach(o -> {
            CacheObject cacheObject = new CacheObject(o);
            cacheObjectMap.put(cacheObject.getMd5Sum(), cacheObject);
        });

        return cacheObjectMap;
    }
}
