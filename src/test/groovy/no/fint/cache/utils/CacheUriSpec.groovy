package no.fint.cache.utils

import spock.lang.Specification

class CacheUriSpec extends Specification {

    void setup() {
        CacheUri.cacheUris.clear()
    }

    def "Get cache uri from model and orgId"() {
        when:
        def cacheUri = CacheUri.create('rogfk.no', 'personalressurs')

        then:
        cacheUri == 'urn:fint.no:rogfk.no:personalressurs'
        CacheUri.cacheUris.size() == 1
    }

    def "Throw IllegalArgumentException if model is null"() {
        when:
        CacheUri.create('rogfk.no', null)

        then:
        thrown(IllegalArgumentException)
    }

}
