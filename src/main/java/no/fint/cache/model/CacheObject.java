package no.fint.cache.model;

import java.io.Serializable;

public interface CacheObject<T extends Serializable> extends Serializable {
    T getObject();

    String getChecksum();

    long getLastUpdated();

    byte[] getBytes();

    int[] getHashCodes();

    int getSize();
}
