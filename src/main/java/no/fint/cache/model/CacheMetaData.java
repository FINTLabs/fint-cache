package no.fint.cache.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CacheMetaData {
    private String md5Sum;
    private Date lastUpdated;
    private int cacheCount;

    public CacheMetaData() {
        md5Sum = null;
        lastUpdated = null;
        cacheCount = 0;
    }
}
