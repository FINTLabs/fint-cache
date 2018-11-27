package no.fint.cache.model;

import java.util.stream.IntStream;

public class DualIndex implements Index {
    private final int value1;
    private final int value2;

    DualIndex(int value1, int value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public IntStream stream() {
        return IntStream.of(value1, value2);
    }

    @Override
    public Index add(int value3) {
        return new MultiIndex(value1, value2, value3);
    }
}
