package no.fint.cache.monitoring;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Data;

import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

@Data
class Measurement {
    private final LongAdder count = new LongAdder();
    private final LongAccumulator totalTimeUsed = new LongAccumulator(Long::sum, 0L);
    private final LongAccumulator min = new LongAccumulator(Long::min, Long.MAX_VALUE);
    private final LongAccumulator max = new LongAccumulator(Long::max, Long.MIN_VALUE);

    public void add(long elapsed) {
        count.increment();
        totalTimeUsed.accumulate(elapsed);
        min.accumulate(elapsed);
        max.accumulate(elapsed);
    }

    @JsonGetter
    public long average() {
        long countSum = count.sum();
        return (countSum == 0) ? 0 : (totalTimeUsed.get() / countSum);
    }

    @JsonGetter
    public long count() {
        return count.sum();
    }

    @JsonGetter
    public long min() {
        return min.get();
    }

    @JsonGetter
    public long max() {
        return max.get();
    }
}
