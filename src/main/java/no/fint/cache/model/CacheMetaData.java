package no.fint.cache.model;

import lombok.Data;

@Data
public class CacheMetaData {
    private byte[] checksum;
    private long lastUpdated;
    private int cacheCount;

    public CacheMetaData() {
        checksum = null;
        lastUpdated = 0;
        cacheCount = 0;
    }
}
