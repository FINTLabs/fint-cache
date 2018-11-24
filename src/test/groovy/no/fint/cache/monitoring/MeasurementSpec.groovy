package no.fint.cache.monitoring

import spock.lang.Specification

class MeasurementSpec extends Specification {
    private Measurement measurement

    void setup() {
        measurement = new Measurement()
    }

    def "Add measurements and get count, min, max and average"() {
        when:
        measurement.add(1)
        measurement.add(2)
        measurement.add(3)

        then:
        measurement.count() == 3
        measurement.min() == 1
        measurement.max() == 3
        measurement.average() == 2
    }

    def "Calculate average given 0 count return 0"() {
        when:
        def average = measurement.average()

        then:
        average == 0
    }
}
