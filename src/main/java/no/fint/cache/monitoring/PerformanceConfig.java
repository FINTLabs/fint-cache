package no.fint.cache.monitoring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "fint.cache.performance-monitor", havingValue = "true")
public class PerformanceConfig {

    @Bean
    public PerformanceMonitor performanceMonitor() {
        return new PerformanceMonitor();
    }

    @Bean
    public PerformanceMonitorController performanceMonitorController() {
        return new PerformanceMonitorController(performanceMonitor());
    }
}
