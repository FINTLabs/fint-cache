package no.fint.cache.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.codec.digest.DigestUtils;

@Getter
@EqualsAndHashCode(of = "checksum")
public class CacheObject<T> {
    private String checksum;
    private long lastUpdated;
    private T object;

    public CacheObject(T obj) {
        object = obj;
        lastUpdated = System.currentTimeMillis();
        checksum = DigestUtils.sha1Hex(object.toString().getBytes());
    }

    CacheObject(T obj, long timestamp) {
        object = obj;
        lastUpdated = timestamp;
        checksum = DigestUtils.sha1Hex(object.toString().getBytes());
    }

}
