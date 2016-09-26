package no.fint.cache.model;

import java.util.Date;
import static org.springframework.util.DigestUtils.md5DigestAsHex;

public class CacheObject {
    private String md5Sum;
    private long lastUpdated;
    private Object object;

    public CacheObject(Object obj) {
        object = obj;
        lastUpdated = System.currentTimeMillis();
        md5Sum = md5DigestAsHex(object.toString().getBytes());
    }

    CacheObject(Object obj, long timestamp) {
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

    public Object getObject() {
        return object;
    }

}
