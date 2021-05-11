package no.fint.cache.utils

import spock.lang.Specification

class RedisConnectionStringSpec extends Specification {
    def 'Parses a connection string with properties'() {
        given:
        def cs = 'fint-cache-beta.redis.cache.windows.net:6380,password=uVPUAlaI58NIwPrAwbxo398PdJAiAvCaA3Gjeg1JY+c=,ssl=True,abortConnect=False'

        when:
        def r = RedisConnectionString.parse(cs)
        println(r.getProperty('password'))

        then:
        r.containsKey('password')
        r.getProperty('ssl') == 'True'
        r.port == 6380
    }
}
