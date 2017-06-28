package no.fint.cache.utils;

import no.fint.cache.CacheService;
import org.springframework.stereotype.Component;

@Component
public class TestCacheService extends CacheService<String> {
    @Override
    protected String getModel() {
        return "test";
    }
}
