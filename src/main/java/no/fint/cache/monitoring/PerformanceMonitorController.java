package no.fint.cache.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/performance-monitor")
public class PerformanceMonitorController {

    private final PerformanceMonitor performanceMonitor;

    PerformanceMonitorController(PerformanceMonitor performanceMonitor) {
        log.info("Initializing performance monitor");
        this.performanceMonitor = performanceMonitor;
    }

    @GetMapping("/keys")
    public List<String> getKeys() {
        return performanceMonitor.getMeasurements().keySet().stream().sorted().collect(Collectors.toList());
    }

    @GetMapping("/measurements")
    public Map<String, Measurement> getMeasurements(@RequestParam(value = "key", required = false) String key) {
        if (key == null) {
            return performanceMonitor.getMeasurements();
        } else {
            return performanceMonitor.getMeasurements().entrySet().stream()
                    .filter(e -> e.getKey().toLowerCase().contains(key.toLowerCase()))
                    .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }

    @DeleteMapping("/measurements")
    public void clearMeasurements() {
        performanceMonitor.clearMeasurements();
    }
}
