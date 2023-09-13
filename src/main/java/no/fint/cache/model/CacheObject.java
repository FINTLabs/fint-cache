package no.fint.cache.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.Serializable;

@Getter
@EqualsAndHashCode(of = "checksum")
@ToString
public final class CacheObject<T extends Serializable> implements CacheObjectType<T> {
    public static Packer PACKER = new SerializationPacker();

    private final byte[] checksum;
    private final long lastUpdated;
    private final byte[] bytes;
    private final int[] hashCodes;
    private final int size;

    @Override
    public T getObject() {
        return (T) PACKER.unpack(bytes);
    }

    public CacheObject(T obj) {
        this(obj, new int[0]);
    }

    public CacheObject(T object, int[] hashes) {
        lastUpdated = System.currentTimeMillis();
        bytes = PACKER.pack(object);
        checksum = DigestUtils.sha1(bytes);
        hashCodes = hashes;
        size = bytes.length;
    }

    @Override
    public String getChecksum() {
        return Hex.encodeHexString(checksum);
    }

}
