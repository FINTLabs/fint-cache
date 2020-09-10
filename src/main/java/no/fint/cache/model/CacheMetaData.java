package no.fint.cache.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class CacheMetaData implements Serializable {
    private long lastUpdated;
    private int cacheCount;
    private long size;

    public CacheMetaData() {
        lastUpdated = 0;
        cacheCount = 0;
        size = 0;
    }
}
