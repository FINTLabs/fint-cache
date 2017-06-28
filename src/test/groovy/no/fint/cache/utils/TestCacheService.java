package no.fint.cache.utils;

import no.fint.cache.CacheService;
import org.springframework.stereotype.Component;

@Component
public class TestCacheService extends CacheService<String> {

    public static final String MODEL = "test";

    public TestCacheService() {
        super(MODEL);
    }

}
