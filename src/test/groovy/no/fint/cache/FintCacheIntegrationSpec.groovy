package no.fint.cache

import no.fint.cache.testutils.TestDto
import org.assertj.core.util.Lists
import spock.lang.Specification

class FintCacheIntegrationSpec extends Specification {

    def "Create a new cache and populate with data"() {
        when:
        List<TestDto> data = Lists.newArrayList(new TestDto())

        FintCache<TestDto> cache = new FintCache<>()
        cache.update(data)
        def cachedList = cache.getSourceList()

        then:
        cachedList.size() == 1
    }

}
