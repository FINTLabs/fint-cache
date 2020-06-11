package no.fint.cache.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.Serializable;
import java.util.function.Supplier;

@Getter
@EqualsAndHashCode(of = "checksum")
@ToString
public final class CacheObject<T extends Serializable> implements Serializable {
    public static Packer PACKER = new SerializationPacker();

    private final byte[] checksum;
    private final long lastUpdated;
    private final Supplier<byte[]> supplier;
    private final int[] hashCodes;
    private final int size;

    public T getObject() {
        return (T) PACKER.unpack(getBytes());
    }

    public byte[] getBytes() {
        return supplier.get();
    }

    public CacheObject(T obj) {
        this(obj, new int[0]);
    }

    public CacheObject(T object, int[] hashes) {
        this(PACKER.pack(object), hashes);
    }

    public CacheObject(byte[] bytes, int[] hashCodes) {
        this(new BytesSupplier(bytes), System.currentTimeMillis(), bytes.length, hashCodes, DigestUtils.sha1(bytes));
    }

    public CacheObject(Supplier<byte[]> supplier, long lastUpdated, int size, int[] hashCodes, byte[] checksum) {
        this.lastUpdated = lastUpdated;
        this.supplier = supplier;
        this.size = size;
        this.hashCodes = hashCodes;
        this.checksum = checksum;
    }

    public String getChecksum() {
        return Hex.encodeHexString(checksum);
    }

    public byte[] rawChecksum() {
        return checksum;
    }

    public static class BytesSupplier implements Supplier<byte[]>, Serializable {

        private final byte[] bytes;

        public BytesSupplier(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public byte[] get() {
            return bytes;
        }
    }
}
