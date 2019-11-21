package no.fint.cache.config;

import no.fint.cache.CacheManager;
import no.fint.cache.FintCacheManager;
import no.fint.cache.HazelcastCacheManager;
import no.fint.cache.model.CacheObject;
import no.fint.cache.model.CompressingPacker;
import no.fint.cache.model.MessagePackPacker;
import no.fint.cache.model.SerializationPacker;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;
import org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStream;
import org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorOutputStream;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FintCacheConfig {

    @Value("${fint.cache.manager:default}")
    private String cacheManagerType;

    @Value("${fint.cache.packer:serialization}")
    private String cachePackerType;

    @Bean
    public CacheManager<?> cacheManager() {
        switch (cachePackerType.toUpperCase()) {
            case "MSGPACK":
                CacheObject.PACKER = new MessagePackPacker();
                break;
            case "LZMA":
                CacheObject.PACKER = new CompressingPacker(Unchecked.function(LZMACompressorOutputStream::new), Unchecked.function(LZMACompressorInputStream::new));
                break;
            case "DEFLATE":
                CacheObject.PACKER = new CompressingPacker(Unchecked.function(DeflateCompressorOutputStream::new), Unchecked.function(DeflateCompressorInputStream::new));
                break;
            case "SNAPPY":
                CacheObject.PACKER = new CompressingPacker(Unchecked.function(FramedSnappyCompressorOutputStream::new), Unchecked.function(FramedSnappyCompressorInputStream::new));
                break;
            default:
                CacheObject.PACKER = new SerializationPacker();
        }

        switch (cacheManagerType.toUpperCase()) {
            case "HAZELCAST":
                return new HazelcastCacheManager<>();
            default:
                return new FintCacheManager<>();
        }
    }
}
