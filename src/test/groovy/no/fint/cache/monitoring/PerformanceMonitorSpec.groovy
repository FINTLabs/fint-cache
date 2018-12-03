package no.fint.cache.monitoring

import no.fint.cache.utils.TestCacheService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = TestApplication, properties = "fint.cache.performance-monitor=true")
class PerformanceMonitorSpec extends Specification {

    @Autowired
    private TestCacheService testCacheService

    @Autowired
    private PerformanceMonitor performanceMonitor

    def "Performance monitor"() {
        when:
        testCacheService.add('rogfk.no', new ArrayList<String>())
        testCacheService.add('rogfk.no', new ArrayList<String>())
        def measurements = performanceMonitor.getMeasurements()
        def measurement = measurements["${TestCacheService.class.getName()}.add"]

        then:
        measurements.size() == 1
        measurement.count() == 2

        when:
        performanceMonitor.clearMeasurements()

        then:
        performanceMonitor.measurements.isEmpty()
    }
}
