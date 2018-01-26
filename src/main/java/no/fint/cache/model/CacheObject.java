package no.fint.cache.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.SerializationUtils;

import java.io.Serializable;

@Getter
@EqualsAndHashCode(of = "checksum")
@ToString
public class CacheObject<T extends Serializable> implements Serializable {
    private byte[] checksum;
    private long lastUpdated;
    private T object;

    public CacheObject(T obj) {
        object = obj;
        lastUpdated = System.currentTimeMillis();
        checksum = DigestUtils.sha1(SerializationUtils.serialize(object));
    }

    CacheObject(T obj, long timestamp) {
        object = obj;
        lastUpdated = timestamp;
        checksum = DigestUtils.sha1(SerializationUtils.serialize(object));
    }

    public String getChecksum() {
        return Hex.encodeHexString(checksum);
    }

    public byte[] rawChecksum() {
        return checksum;
    }

}
