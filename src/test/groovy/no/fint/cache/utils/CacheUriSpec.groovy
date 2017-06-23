package no.fint.cache.utils

import spock.lang.Specification

class CacheUriSpec extends Specification {

    def "Get cache uri from model and orgId"() {
        when:
        def cacheUri = CacheUri.create('rogfk.no', 'personalressurs')

        then:
        cacheUri == 'urn:fint.no:rogfk.no:personalressurs'
    }

}
