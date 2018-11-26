package no.fint.cache.monitoring

import com.google.common.math.StatsAccumulator
import org.apache.commons.math3.stat.descriptive.moment.Variance
import spock.lang.Specification

import java.util.concurrent.ThreadLocalRandom

import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.that

class MeasurementSpec extends Specification {
    def "Add measurements and get count, min, max and average"() {
        given:
        def measurement = new Measurement(1)

        when:
        measurement.add(2).add(3)

        then:
        measurement.count() == 3
        measurement.min() == 1
        measurement.max() == 3
        measurement.mean() == 2
        measurement.variance() == 1
    }

    def "Calculate variance"() {
        given:
        def numbers = [num(), num(), num(), num(), num()] as double[]
        def measurement = new Measurement(numbers[0])
                .add(numbers[1])
                .add(numbers[2])
                .add(numbers[3])
                .add(numbers[4])

        when:
        def variance = measurement.variance()
        def expected = new Variance().evaluate(numbers).doubleValue()

        then:
        that variance, closeTo(expected, 0.01)
    }

    private static double num() {
        ThreadLocalRandom.current().nextDouble(1000)
    }

    def "Compare with StatsAccumulator"() {
        given:
        def acc = new StatsAccumulator()
        def measurement = new Measurement(1)

        when:
        acc.add(1)
        acc.add(2)
        acc.add(3)
        measurement.add(2).add(3)

        then:
        acc.count() == measurement.count()
        acc.min() == measurement.min()
        acc.max() == measurement.max()
        acc.mean() == measurement.mean()
    }
}
