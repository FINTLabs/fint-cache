package no.fint.cache.model;

import java.util.stream.IntStream;

public class SingleIndex implements Index {

    private final int value;

    public SingleIndex(int value) {
        this.value = value;
    }

    @Override
    public IntStream stream() {
        return IntStream.of(value);
    }

    @Override
    public Index add(int value) {
        return new DualIndex(this.value, value);
    }
}
