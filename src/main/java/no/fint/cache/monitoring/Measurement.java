package no.fint.cache.monitoring;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Data;

import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.concurrent.atomic.LongAdder;

@Data
class Measurement {

    private final double initial;
    private final LongAdder count = new LongAdder();
    private final DoubleAccumulator sum;
    private final DoubleAccumulator sumSq;
    private final DoubleAccumulator min;
    private final DoubleAccumulator max;

    public Measurement(double initial) {
        this.initial = initial;
        sum = new DoubleAccumulator((a, b) -> a + (b - initial), 0);
        sumSq = new DoubleAccumulator((a, b) -> a + (b - initial) * (b - initial), 0);
        min = new DoubleAccumulator(Double::min, initial);
        max = new DoubleAccumulator(Double::max, initial);
        count.increment();
        sum.accumulate(initial);
        sumSq.accumulate(initial);
    }

    public Measurement add(double elapsed) {
        count.increment();
        sum.accumulate(elapsed);
        sumSq.accumulate(elapsed);
        min.accumulate(elapsed);
        max.accumulate(elapsed);
        return this;
    }

    @JsonGetter
    public double mean() {
        return initial + sum.get() / count.sum();
    }

    @JsonGetter
    public double variance() {
        double sum = this.sum.get();
        long count = this.count.sum();
        return (sumSq.get() - sum * sum / count) / (count - 1);
    }

    @JsonGetter
    public long count() {
        return count.sum();
    }

    @JsonGetter
    public double min() {
        return min.get();
    }

    @JsonGetter
    public double max() {
        return max.get();
    }
}
