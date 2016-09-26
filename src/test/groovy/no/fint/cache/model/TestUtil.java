package no.fint.cache.model;

public enum TestUtil {
    ;

    public static CacheObject createCacheObject(Object obj, long timestamp) {
        return new CacheObject(obj, timestamp);
    }
}
