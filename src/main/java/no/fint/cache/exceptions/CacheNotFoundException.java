package no.fint.cache.exceptions;

import java.util.NoSuchElementException;

public class CacheNotFoundException extends NoSuchElementException {
    public CacheNotFoundException(String s) {
        super(s);
    }
}
