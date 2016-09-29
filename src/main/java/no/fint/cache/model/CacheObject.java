package no.fint.cache.model;

import static org.springframework.util.DigestUtils.md5DigestAsHex;

public class CacheObject<T> {
    private String md5Sum;
    private long lastUpdated;
    private T object;

    public CacheObject(T obj) {
        object = obj;
        lastUpdated = System.currentTimeMillis();
        md5Sum = md5DigestAsHex(object.toString().getBytes());
    }

    CacheObject(T obj, long timestamp) {
        object = obj;
        lastUpdated = timestamp;
        md5Sum = md5DigestAsHex(object.toString().getBytes());
    }

    public String getMd5Sum() {
        return md5Sum;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public T getObject() {
        return object;
    }

}
