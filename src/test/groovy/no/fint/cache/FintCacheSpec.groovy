package no.fint.cache

import no.fint.cache.model.CacheMetaData
import no.fint.cache.model.CacheObject
import no.fint.cache.model.TestUtil
import spock.lang.Specification

import java.time.LocalDateTime


class FintCacheSpec extends Specification {
    private FintCache defaultCache

    void setup() {
        defaultCache = new FintCache()
    }

    def "Update cache, no existing values"() {
        given:
        def values = ["123": new CacheObject("test-value")]

        when:
        defaultCache.update(values)

        then:
        defaultCache.cacheMetaData.cacheCount == 1
    }

    def "Update cache, add new object"() {
        given:
        def cacheObj1 = new CacheObject("test-value1")
        def values = new HashMap<>()
        values.put(cacheObj1.getMd5Sum(), cacheObj1)
        defaultCache.update(values)

        def cacheObj2 = new CacheObject("test-value2")
        values.put(cacheObj2.getMd5Sum(), cacheObj2)

        when:
        defaultCache.update(values)

        then:
        defaultCache.cacheMetaData.cacheCount == 2
    }

    def "Update cache, remove existing object"() {
        given:
        def cacheObj1 = new CacheObject("test-value1")
        def cacheObj2 = new CacheObject("test-value2")
        def cacheObj3 = new CacheObject("test-value3")
        def values = new HashMap<>()
        values.put(cacheObj1.getMd5Sum(), cacheObj1)
        values.put(cacheObj2.getMd5Sum(), cacheObj2)
        values.put(cacheObj3.getMd5Sum(), cacheObj3)
        defaultCache.update(values)

        when:
        values.remove(cacheObj3.getMd5Sum())
        defaultCache.update(values)

        then:
        defaultCache.cacheMetaData.cacheCount == 2
    }

    def "Update cache, update content of existing object"() {
        given:
        def cacheObj1 = new CacheObject("test-value1")
        def cacheObj2 = new CacheObject("test-value2")
        def cacheObj3 = new CacheObject("test-value3")
        def values = new HashMap<>()
        values.put(cacheObj1.getMd5Sum(), cacheObj1)
        values.put(cacheObj2.getMd5Sum(), cacheObj2)
        values.put(cacheObj3.getMd5Sum(), cacheObj3)
        defaultCache.update(values)

        when:
        values.remove(cacheObj3.getMd5Sum())
        def cacheObj4 = new CacheObject("test-value4")
        values.put(cacheObj4.getMd5Sum(), cacheObj4)
        defaultCache.update(values)

        then:
        defaultCache.cacheMetaData.cacheCount == 3
    }

    def "Refresh cache"() {
        given:
        def cacheObj = new CacheObject("test-value")
        def values = new HashMap()
        values.put(cacheObj.getMd5Sum(), cacheObj)

        when:
        defaultCache.refresh(values)

        then:
        defaultCache.cacheMetaData.cacheCount == 1
    }

    def "Flush cache"() {
        given:
        def cacheObj = new CacheObject("test-value")
        def values = new HashMap()
        values.put(cacheObj.getMd5Sum(), cacheObj)
        defaultCache.update(values)

        when:
        defaultCache.flush()

        then:
        defaultCache.cacheMetaData.cacheCount == 0
        defaultCache.cacheMetaData.lastUpdated == 0
        defaultCache.cacheMetaData.md5Sum == null
    }

    def "Get updated cache objects since timestamp, new objects"() {
        given:
        def cacheObj = TestUtil.createCacheObject("test-value", System.currentTimeMillis() - 10000)
        def values = new HashMap()
        values.put(cacheObj.getMd5Sum(), cacheObj)
        defaultCache.update(values)

        when:
        def updatedSince = defaultCache.getSince(System.currentTimeMillis() - 15000)

        then:
        updatedSince.size() == 1
    }

    def "Get updated cache objects since timestamp, no new objects"() {
        given:
        def cacheObj = TestUtil.createCacheObject("test-value", System.currentTimeMillis() - 10000)
        def values = new HashMap()
        values.put(cacheObj.getMd5Sum(), cacheObj)
        defaultCache.update(values)

        when:
        def updatedSince = defaultCache.getSince(System.currentTimeMillis() + 15000)

        then:
        updatedSince.size() == 0
    }

    def "Get cache"() {
        given:
        def cacheObj = new CacheObject("test-value")
        def values = new HashMap()
        values.put(cacheObj.getMd5Sum(), cacheObj)
        defaultCache.update(values)

        when:
        def cachedValues = defaultCache.get()

        then:
        cachedValues[0] == values.values()[0]
    }
}
