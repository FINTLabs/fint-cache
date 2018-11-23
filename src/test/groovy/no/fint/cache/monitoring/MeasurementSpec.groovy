package no.fint.cache.monitoring

import spock.lang.Specification

class MeasurementSpec extends Specification {

    def "Add measurements and get count, min, max and average"() {
        given:
        Measurement measurement = new Measurement()

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
}
