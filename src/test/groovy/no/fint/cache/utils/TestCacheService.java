package no.fint.cache.utils;

import no.fint.cache.CacheService;
import no.fint.cache.testutils.TestAction;
import no.fint.event.model.Event;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TestCacheService extends CacheService<String> {

    public static final String MODEL = "test";

    public TestCacheService() {
        super(MODEL, TestAction.SUPPORTED_ACTION_1, TestAction.SUPPORTED_ACTION_2);
    }

    public Optional<String> getOne(String orgId, String id) {
        return super.getOne(orgId, (value) -> value.equals(id));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onAction(Event event) {
        update(event.getOrgId(), event.getData());
    }

    @Override
    public void populateCache(String orgId) {
        
    }
}
