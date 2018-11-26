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
    private int[] hashCodes;

    public CacheObject(T obj) {
        this(obj, new int[0]);
    }

    public CacheObject(T obj, int[] hashes) {
        object = obj;
        lastUpdated = System.currentTimeMillis();
        checksum = DigestUtils.sha1(SerializationUtils.serialize(object));
        hashCodes = hashes;
    }

    CacheObject(T obj, long timestamp) {
        object = obj;
        lastUpdated = timestamp;
        checksum = DigestUtils.sha1(SerializationUtils.serialize(object));
        hashCodes = new int[0];
    }

    public String getChecksum() {
        return Hex.encodeHexString(checksum);
    }

    public byte[] rawChecksum() {
        return checksum;
    }

}
