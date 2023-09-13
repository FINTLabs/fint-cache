package no.fint.cache.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.codec.binary.Hex;

import java.io.Serializable;

@Getter
@EqualsAndHashCode(of = "checksum")
@ToString
public class PojoCacheObject<T extends Serializable> implements CacheObjectType<T> {

    private final byte[] checksum;
    private final long lastUpdated;
    private final T resource;
    private final int[] hashCodes;
    private final int size;

    @Override
    public T getObject() {
        return resource;
    }

    public PojoCacheObject(T obj) {
        this(obj, new int[0]);
    }

    public PojoCacheObject(T object, int[] hashes) {
        lastUpdated = System.currentTimeMillis();
        resource = object;
        checksum = new byte[0];
        hashCodes = hashes;
        size = 0;
    }

    @Override
    public String getChecksum() {
        return Hex.encodeHexString(checksum);
    }

    @Override
    public byte[] getBytes() {
        return new byte[0];
    }
}
