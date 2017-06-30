package no.fint.cache.utils

import no.fint.cache.CacheService
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

    def "Throw IllegalArgumentException if cacheUri is empty"() {
        when:
        CacheUri.containsOrgId('', 'rogfk.no')

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

    def "Get all cacheUris for multiple CacheServices"() {
        given:
        def cacheService1 = Mock(CacheService)
        def cacheService2 = Mock(CacheService)

        when:
        def cacheUris = CacheUri.getCacheUris([cacheService1, cacheService2])

        then:
        1 * cacheService1.getKeys() >> [CacheUri.create('rogfk.no', 'test')]
        1 * cacheService2.getKeys() >> [CacheUri.create('hfk.no', 'test')]
        cacheUris.size() == 2
    }

    def "Return false if CacheService returns null for keys"() {
        given:
        def cacheService = Mock(CacheService)

        when:
        def containsOrgId = CacheUri.containsOrgId([cacheService], 'rogfk.no')

        then:
        1 * cacheService.getKeys() >> null
        !containsOrgId
    }

    def "Return true if CacheService contains the orgId"() {
        given:
        def cacheService = Mock(CacheService)

        when:
        def containsOrgId = CacheUri.containsOrgId([cacheService], 'rogfk.no')

        then:
        1 * cacheService.getKeys() >> [CacheUri.create('rogfk.no', 'test123')]
        containsOrgId
    }
}
