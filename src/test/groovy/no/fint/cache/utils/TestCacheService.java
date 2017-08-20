package no.fint.cache.utils;

import no.fint.cache.CacheService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TestCacheService extends CacheService<String> {

    public static final String MODEL = "test";

    public TestCacheService() {
        super(MODEL);
    }

    public Optional<String> getOne(String orgId, String id) {
        return super.getOne(orgId, (value) -> value.equals(id));
    }
}
