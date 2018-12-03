package no.fint.cache.monitoring

import no.fint.test.utils.MockMvcSpecification
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers

class PerformanceMonitorControllerSpec extends MockMvcSpecification {
    private PerformanceMonitorController controller
    private PerformanceMonitor performanceMonitor
    private MockMvc mockMvc

    void setup() {
        Map<String, Measurement> measurements = [
                'key1': new Measurement(1),
                'key2': new Measurement(2)
        ]

        performanceMonitor = Mock(PerformanceMonitor) {
            getMeasurements() >> measurements
        }
        controller = new PerformanceMonitorController(performanceMonitor)
        mockMvc = standaloneSetup(controller)
    }

    def "Get all keys"() {
        when:
        def response = mockMvc.perform(get('/performance-monitor/keys'))

        then:
        response
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPathSize('$', 2))
                .andExpect(jsonPathEquals('$[0]', 'key1'))
                .andExpect(jsonPathEquals('$[1]', 'key2'))
    }

    def "Get measurements"() {
        when:
        def response = mockMvc.perform(get('/performance-monitor/measurements'))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.key1.count').value(equalTo(1)))
                .andExpect(jsonPath('$.key2.count').value(equalTo(1)))
    }

    def "Get measurements with key containing key1"() {
        when:
        def response = mockMvc.perform(get('/performance-monitor/measurements?key=key1'))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.key1.count').value(equalTo(1)))
                .andExpect(jsonPath('$.key2').doesNotExist())
    }

    def "Clear measurements"() {
        when:
        def response = mockMvc.perform(delete('/performance-monitor/measurements'))

        then:
        1 * performanceMonitor.clearMeasurements()
        response.andExpect(status().isOk())
    }
}
