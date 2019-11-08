package no.fint.cache.model;

import java.io.Serializable;
import java.util.stream.IntStream;

public interface Index extends Serializable {
    IntStream stream();
    Index add(int value);
}
