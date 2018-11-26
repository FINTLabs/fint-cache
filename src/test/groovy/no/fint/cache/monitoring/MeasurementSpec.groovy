package no.fint.cache.monitoring

import com.google.common.math.StatsAccumulator
import spock.lang.Specification

class MeasurementSpec extends Specification {
    def "Add measurements and get count, min, max and average"() {
        given:
        def measurement = new Measurement(1)

        when:
        measurement.add(2)
        measurement.add(3)

        then:
        measurement.count() == 3
        measurement.min() == 1
        measurement.max() == 3
        measurement.mean() == 2
    }

    def "Compare with StatsAccumulator"() {
        given:
        def acc = new StatsAccumulator()

        when:
        acc.add(1)
        acc.add(2)
        acc.add(3)

        then:
        acc.count() == 3
        acc.min() == 1
        acc.max() == 3
        acc.mean() == 2
    }
}
