package no.fint.cache.monitoring;

import no.fint.cache.annotations.EnableFintCache;
import no.fint.cache.utils.TestCacheService;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableFintCache
@SpringBootApplication(scanBasePackageClasses = TestCacheService.class)
public class TestApplication {
}
