package no.fint.cache.model

import no.fint.cache.testutils.TestDto
import spock.lang.Specification

import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream

class PackerSpec extends Specification {

    def 'Validate deflate packer'() {
        given:
        def packer = new CompressingPacker({OutputStream o -> new DeflaterOutputStream(o)}, { InputStream i -> new InflaterInputStream(i)})
        def obj = new TestDto(id: 'Some random object')

        when:
        def data = packer.pack(obj)
        def obj2 = packer.unpack(data) as TestDto

        then:
        obj == obj2
    }

    def 'Deflater actually deflates'() {
        given:
        def packer1 = PackerFactory.create("deflate")
        def packer2 = PackerFactory.create("default")
        def obj = new TestDto(id: 'Some random object with some random data that some random compression could do some random magic to!')

        when:
        def data1 = packer1.pack(obj)
        def data2 = packer2.pack(obj)

        then:
        data1.length < data2.length
    }
}
