package no.fint.cache

import no.fint.cache.model.CacheObject
import spock.lang.Specification

import java.util.stream.Collectors

class FintCacheSpec extends Specification {
    private FintCache defaultCache

    void setup() {
        defaultCache = new FintCache()
    }

    def 'Update cache with empty input'() {
        when:
        defaultCache.update([])

        then:
        noExceptionThrown()
    }

    def "Update cache, no existing values"() {
        given:
        def values = ["test-value"]

        when:
        defaultCache.update(values)

        then:
        defaultCache.size() == 1
    }

    def "Update cache, add new object"() {
        given:
        def cacheObj1 = 'test-value1'
        def values = new ArrayList()
        values.add(cacheObj1);
        defaultCache.update(values)

        def cacheObj2 = 'test-value2'
        values.add(cacheObj2)

        when:
        defaultCache.update(values)

        then:
        defaultCache.size() == 2
    }

    def "Update cache, remove existing object"() {
        given:
        def cacheObj1 = 'test-value1'
        def cacheObj2 = 'test-value2'
        def cacheObj3 = 'test-value3'
        def values = new ArrayList()
        values.add(cacheObj1)
        values.add(cacheObj2)
        values.add(cacheObj3)
        defaultCache.update(values)

        when:
        values.remove(cacheObj3)
        defaultCache.update(values)

        then:
        defaultCache.size() == 2
    }

    def "Update cache, update content of existing object"() {
        given:
        def cacheObj1 = 'test-value1'
        def cacheObj2 = 'test-value2'
        def cacheObj3 = 'test-value3'
        def values = new ArrayList()
        values.add(cacheObj1)
        values.add(cacheObj2)
        values.add(cacheObj3)
        defaultCache.update(values)

        when:
        values.remove(cacheObj3)
        def cacheObj4 = 'test-value4'
        values.add(cacheObj4)
        defaultCache.update(values)

        then:
        defaultCache.size() == 3
    }

    def "Update cache, existing content and empty update"() {
        given:
        def cacheObj = 'test-value'
        def values = new ArrayList()
        values.add(cacheObj)
        defaultCache.update(values)

        when:
        defaultCache.update([])

        then:
        defaultCache.size() == 1
    }

    def "Flush cache"() {
        given:
        def cacheObj = 'test-value'
        def values = new ArrayList()
        values.add(cacheObj)
        defaultCache.update(values)

        when:
        defaultCache.flush()

        then:
        defaultCache.size() == 0
        defaultCache.cacheMetaData.lastUpdated == 0
        defaultCache.cacheMetaData.checksum == null
    }

    def "Get updated cache objects since timestamp, new objects"() {
        given:
        def cacheObj = 'test-value'
        def values = new ArrayList()
        values.add(cacheObj)
        defaultCache.update(values)

        when:
        def updatedSince = defaultCache.getSince(System.currentTimeMillis() - 15000)

        then:
        updatedSince.count() == 1
    }

    def "Get updated cache objects since timestamp, no new objects"() {
        given:
        def cacheObj = 'test-value'
        def values = new ArrayList()
        values.add(cacheObj)
        defaultCache.update(values)

        when:
        def updatedSince = defaultCache.getSince(System.currentTimeMillis() + 15000)

        then:
        updatedSince.count() == 0
    }

    def "Get cache"() {
        given:
        def cacheObj = 'test-value'
        def values = new ArrayList()
        values.add(cacheObj)
        defaultCache.update(values)

        when:
        def cachedValues = defaultCache.get()

        then:
        cachedValues.findAny().get().getObject() == values.get(0)
    }

    def "Get source list"() {
        given:
        def cacheObj = 'test-value'
        def values = new ArrayList()
        values.add(cacheObj)
        defaultCache.update(values)

        when:
        def sourceList = defaultCache.getSourceList()

        then:
        sourceList.get(0) == cacheObj
    }

    def "Get updated source objects since timestamp, new objects"() {
        given:
        def cacheObj = 'test-value'
        def values = new ArrayList()
        values.add(cacheObj)
        defaultCache.update(values)

        when:
        def updatedSince = defaultCache.getSourceListSince(System.currentTimeMillis() - 15000)

        then:
        updatedSince.size() == 1
        updatedSince.get(0) == cacheObj
    }

    def "Get updated source objects since timestamp, no new objects"() {
        given:
        def cacheObj = 'test-value'
        def values = new ArrayList()
        values.add(cacheObj)
        defaultCache.update(values)

        when:
        def updatedSince = defaultCache.getSourceListSince(System.currentTimeMillis() + 15000)

        then:
        defaultCache.getLastUpdated() > 0
        updatedSince.size() == 0
    }

    def 'Populate cache and index' () {
        given:
        def l = []
        def i = 0
        def cacheobjects = ['some', 'data', 'to', 'cache'].collect { l += ++i; new CacheObject<>(it, l as int[])}
        defaultCache.updateCache(cacheobjects)

        when:
        def item = defaultCache.filter(1, { it -> (it == 'some') } ).collect(Collectors.toList())

        then:
        item
        item.size() == 1
        item.every { (it.getObject() == 'some') }

        when:
        item = defaultCache.filter(3, { it -> (it == 'cache') } ).collect(Collectors.toList())

        then:
        item
        item.size() == 1
        item.every { (it.getObject() == 'cache') }

        when:
        item = defaultCache.filter(4, { it -> (it == 'cache') } ).collect(Collectors.toList())

        then:
        item
        item.size() == 1
        item.every { (it.getObject() == 'cache') }
    }
}
