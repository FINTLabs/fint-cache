package no.fint.cache.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class MultiIndex implements Index {
    private final List<Integer> values = new ArrayList<>();

    MultiIndex(int value1, int value2, int value3) {
        values.add(value1);
        values.add(value2);
        values.add(value3);
    }

    @Override
    public IntStream stream() {
        return values.stream().mapToInt(Integer::intValue);
    }

    @Override
    public Index add(int value) {
        values.add(value);
        return this;
    }
}
