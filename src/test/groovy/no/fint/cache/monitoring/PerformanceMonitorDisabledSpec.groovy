package no.fint.cache.monitoring

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = TestApplication)
class PerformanceMonitorDisabledSpec extends Specification {

    @Autowired(required = false)
    private PerformanceMonitor performanceMonitor

    @Autowired(required = false)
    private PerformanceMonitorController performanceMonitorController

    def "Disable performance monitor by default"() {
        expect:
        !performanceMonitor
        !performanceMonitorController
    }
}
