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

    public PojoCacheObject(CacheObject<T> cacheObject) {
        lastUpdated = System.currentTimeMillis();
        resource = cacheObject.getObject();
        this.checksum = cacheObject.getChecksumRaw();
        hashCodes = cacheObject.getHashCodes();
        size = 0;
    }


    public PojoCacheObject(T object) {

        CacheObject<T> cacheObject = new CacheObject<>(object, new int[]{object.hashCode()});
        lastUpdated = System.currentTimeMillis();
        resource = cacheObject.getObject();
        this.checksum = cacheObject.getChecksumRaw();
        hashCodes = cacheObject.getHashCodes();
        size = 0;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        final PojoCacheObject other = (PojoCacheObject) object;

        if (this.resource == null && other.getResource() == null) return true;
        if (this.resource == null || other.getResource() == null) return false;

        return this.resource.equals(other.getResource());
    }

    @Override
    public int hashCode() {
        return resource.hashCode();
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
