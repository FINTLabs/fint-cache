package no.fint.cache

import no.fint.cache.model.CacheObject
import org.junit.Ignore;
import spock.lang.Specification

import java.util.stream.Collectors

class FintCacheSpec2 extends Specification {

    def cache

    void setup() {
        cache = new FintCache<TestObject>()
    }

    def "Construct new cache"() {
        expect:
        cache.size() == 0
        cache.flush()
    }

    def "Add element to cache"() {
        given:
        def testObject = new TestObject("Frodo Lommelun");

        when:
        cache.add([testObject])

        then:
        cache.size() == 1
        testObject.equals(cache.stream().findFirst().get().getObject())
    }

    def "Add multiple elements to cache"() {
        when:
        cache.add([new TestObject("Samvis Gamgod"), new TestObject("Gandalv"), new TestObject("Tom Bombadil")])

        then:
        cache.size() == 3
    }

    def "Update two elements"() {
        when:
        cache.add([new TestObject("Bilbo Lommelun")])
        cache.add([new TestObject("Frodo Lommelun")])

        then:
        cache.size() == 2
    }

    @Ignore("Denne feiler. Burde ikke oppdatere lastUpdated om de ikke er noen endring")
    def "Don't update if the element is unchanged"() {
        given:
        def testObject = new TestObject("Frodo Lommelun");

        when:
        cache.add([testObject])
        def firstChange = cache.lastUpdated
        sleep(10)
        cache.update([testObject])
        def lastChange = cache.lastUpdated

        then:
        cache.size() == 1
        firstChange == lastChange
    }

    @Ignore("Core1-cache bevarer ikke naturlig orden, men sorterer på checksum")
    def "Preserve insertion-order"() {
        when:
        cache.add([new TestObject("Samvis Gamgod")])
        cache.add([new TestObject("Gandalv")])
        cache.add([new TestObject("Tom Bombadil")])
        cache.add([new TestObject("Arwen")])
        cache.add([new TestObject("Gollum")])

        then:
        def values = cache.stream().map(l -> l.getObject()).collect(Collectors.toList());
        values.get(0).name == "Samvis Gamgod";
        values.get(1).name == "Gandalv";
        values.get(2).name == "Tom Bombadil";
        values.get(3).name == "Arwen";
        values.get(4).name == "Gollum";
    }

    @Ignore("Usikker på hvorfor. Bør rettes")
    def "Filter element by hashCode"() {
        given:
        def hashCode = 123456789
        cache.addCache([new CacheObject<>(new TestObject("Samvis Gamgod"))])
        cache.addCache([new CacheObject<>(new TestObject("Gandalv"), new int[] {hashCode})])
        cache.addCache([new CacheObject<>(new TestObject("Tom Bombadil"))])

        when:
        def result = cache.filter(hashCode, () -> true).map(l -> l.getObject()).collect(Collectors.toList())

        then:
        result.size() == 1
        result.get(0) == new TestObject("Gandalv")
    }

    def "Filter element by since"() {
        when:
        cache.add([new TestObject("Samvis Gamgod")])
        cache.add([new TestObject("Gandalv")])
        cache.add([new TestObject("Tom Bombadil")])
        def lastUpdate = cache.getLastUpdated()
        Thread.sleep(1)
        cache.add([new TestObject("Arwen")])
        cache.add([new TestObject("Gollum")])

        then:
        cache.streamSince(lastUpdate).count() == 2

        // before lock on getLastUpdated & put, we expreienced this test to failed every 3-5 execution
        // probably because getLastUpdated and put was executed in the same milli secound
        where:
        i << (1..10)
    }

    def "Filter element by predicate"() {
        given:
        cache.add([new TestObject("Samvis Gamgod")])
        cache.add([new TestObject("Gandalv")])
        cache.add([new TestObject("Tom Bombadil")])
        cache.add([new TestObject("Arwen")])
        cache.add([new TestObject("Gollum")])

        when:
        def rigthPerson = "Gandalv"

        def result = cache.filter(resource -> Optional
                .ofNullable(resource)
                .map(TestObject::getName)
                .map(rigthPerson::equals)
                .orElse(false)
            ).map(l->l.getObject()).collect(Collectors.toList())

        then:
        result.get(0).name.equals("Gandalv")
        result.size() == 1
    }

    def "Remove all elements from cache"() {
        given:
        cache.add([new TestObject("Samvis Gamgod")])
        cache.add([new TestObject("Gandalv")])
        cache.add([new TestObject("Tom Bombadil")])

        when:
        cache.flush()

        then:
        cache.size() == 0
    }
}