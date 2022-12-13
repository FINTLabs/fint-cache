package no.fint.cache

import no.fint.cache.exceptions.CacheNotFoundException
import no.fint.cache.model.ByteArrayCacheObject
import no.fint.cache.testutils.TestAction
import no.fint.cache.utils.CacheUri
import no.fint.cache.utils.TestCacheService
import no.fint.event.model.Event
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import java.util.stream.Collectors

@SpringBootTest(classes = [ FintCacheManager, TestCacheService ])
class FintCacheIntegrationSpec extends Specification {
    @Autowired
    private TestCacheService testCacheService

    void setup() {
        testCacheService.createCache('rogfk.no')
        testCacheService.update('rogfk.no', ['test1', 'test2'])
    }

    void cleanup() {
        testCacheService.remove('rogfk.no')
    }

    def "Create cache"() {
        when:
        def cache = testCacheService.createCache('rogfk.no')

        then:
        testCacheService.remove('rogfk.no')
        cache != null
        cache.size() == 0
        cache.volume() == 0
    }

    def "Get all values from cache"() {
        when:
        def values = testCacheService.getAll('rogfk.no')

        then:
        testCacheService.hasItems()
        values.size() == 2
        values.contains('test1')
        values.contains('test2')
    }

    def "Throw exception when the cache is not present"() {
        when:
        testCacheService.getAll('unknown-org', System.currentTimeMillis())

        then:
        thrown(CacheNotFoundException)
    }

    def "Add items to cache"() {
        when:
        testCacheService.add('rogfk.no', ['test3'])
        def values = testCacheService.getAll('rogfk.no')

        then:
        values.size() == 3
    }

    def "Adding existing item to cache adds duplicate"() {
        when:
        testCacheService.add('rogfk.no', ['test2', 'test3'])
        def values = testCacheService.getAll('rogfk.no')

        then:
        values.size() == 4
    }

    def "Get all values since timestamp"() {
        when:
        def values = testCacheService.getAll('rogfk.no', System.currentTimeMillis() - 500)

        then:
        values.size() == 2
        values.contains('test1')
        values.contains('test2')
    }

    def "Get one value"() {
        when:
        def one = testCacheService.getOne('rogfk.no', 'test1')

        then:
        one.isPresent()
        one.get() == 'test1'
    }

    def "Return no values when there are no updates since timestamp"() {
        when:
        def values = testCacheService.getAll('rogfk.no', System.currentTimeMillis() + 500)

        then:
        values.size() == 0
    }

    def "Get last updated"() {
        when:
        def lastUpdated = testCacheService.getLastUpdated('rogfk.no')

        then:
        lastUpdated <= System.currentTimeMillis()
        lastUpdated > 0L
    }

    def "Get cache size"() {
        when:
        def size = testCacheService.getCacheSize('rogfk.no')

        then:
        size == 2
    }

    def "Update cache, add new value"() {
        when:
        testCacheService.update('rogfk.no', ['test1', 'test2', 'test3'])
        def values = testCacheService.getAll('rogfk.no')

        then:
        values.size() == 3
        values.contains('test3')
    }

    def "Get keys"() {
        when:
        def keys = testCacheService.getKeys()

        then:
        keys.size() == 1
        keys[0] == CacheUri.create('rogfk.no', 'test')
    }

    def "Flush cache"() {
        when:
        testCacheService.flush('rogfk.no')
        def values = testCacheService.getAll('rogfk.no')

        then:
        values.size() == 0
    }

    def "Supported actions"() {
        when:
        def supportedAction1 = testCacheService.supportsAction(TestAction.SUPPORTED_ACTION_1)
        def supportedAction2 = testCacheService.supportsAction(TestAction.SUPPORTED_ACTION_2)

        then:
        supportedAction1
        supportedAction2
    }

    def "Supported string action"() {
        when:
        def supportedAction = testCacheService.supportsAction(TestAction.SUPPORTED_ACTION_1.name())

        then:
        supportedAction
    }

    def "Not-supported action"() {
        when:
        def supportedAction = testCacheService.supportsAction(TestAction.NOT_SUPPORTED_ACTION)

        then:
        !supportedAction
    }

    def "On received event"() {
        given:
        def event = new Event(orgId: 'rogfk.no', data: ['test1', 'test2', 'test3', 'test4'])

        when:
        testCacheService.onAction(event)
        def values = testCacheService.getAll('rogfk.no')

        then:
        values.size() == 4
    }

    def 'Update cache with hashcodes'() {
        when:
        def values = testCacheService.getAll('rogfk.no')
        testCacheService.updateCache('rogfk.no', values.collect { new ByteArrayCacheObject<>(it, [1] as int[])})

        then:
        testCacheService.hasItems()

        when:
        def result = testCacheService.getOne('rogfk.no', 1, { it == 'test1'})

        then:
        result
        result.isPresent()
        result.get() == 'test1'

        when:
        testCacheService.addCache('rogfk.no', [new ByteArrayCacheObject<String>('test4', [4] as int[])])
        result = testCacheService.getOne('rogfk.no', 4, { true })

        then:
        result.get() == 'test4'
    }

    def 'Stream cache slice'() {
        given:
        testCacheService.update('rogfk.no', ['test1', 'test2', 'test3', 'test4'])

        when:
        def result = testCacheService.streamSlice('rogfk.no', 1, 1).collect(Collectors.toList())

        then:
        result == ['test4']
    }

    def 'Stream cache slice since'() {
        given:
        testCacheService.update('rogfk.no', ['test1', 'test2', 'test3', 'test4'])

        when:
        def result = testCacheService.streamSliceSince('rogfk.no', System.currentTimeMillis() - 500, 1, 1).collect(Collectors.toList())

        then:
        result.size() == 1
        result.every { it.startsWith('test')}
    }
}
