package no.fint.cache.monitoring


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = TestApplication, properties = "fint.cache.performance-monitor=true")
class PerformanceMonitorEnabledSpec extends Specification {

    @Autowired(required = false)
    private PerformanceMonitor performanceMonitor

    @Autowired(required = false)
    private PerformanceMonitorController performanceMonitorController

    def "Enable performance monitor when property set"() {
        expect:
        performanceMonitor
        performanceMonitorController
    }
}
