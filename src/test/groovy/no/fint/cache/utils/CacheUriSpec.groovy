package no.fint.cache.utils

import spock.lang.Specification

class CacheUriSpec extends Specification {

    def "Get cache uri from model and orgId"() {
        when:
        def cacheUri = CacheUri.create('rogfk.no', 'personalressurs')

        then:
        cacheUri == 'urn:fint.no:rogfk.no:personalressurs'
    }

    def "Throw IllegalArgumentException if model is null"() {
        when:
        CacheUri.create('rogfk.no', null)

        then:
        thrown(IllegalArgumentException)
    }

    def "Throw IllegalArgumentException if cacheUri is null"() {
        when:
        CacheUri.containsOrgId(null, 'rogfk.no')

        then:
        thrown(IllegalArgumentException)
    }

    def "Throw IllegalArgumentException if the cacheUri is in an invalid format"() {
        when:
        CacheUri.containsOrgId('invalid cache uri', 'rogfk.no')

        then:
        thrown(IllegalArgumentException)
    }

    def "Return true if cacheUri input contains the orgId"() {
        given:
        def cacheUri = CacheUri.create('rogfk.no', 'personalressurs')

        when:
        def containsOrgId = CacheUri.containsOrgId(cacheUri, 'rogfk.no')

        then:
        containsOrgId
    }
}
