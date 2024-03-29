package no.fint.cache.config;

import no.fint.cache.CacheManager;
import no.fint.cache.FintCacheManager;
import no.fint.cache.model.CacheObject;
import no.fint.cache.model.PackerFactory;
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
        CacheObject.PACKER = PackerFactory.create(cachePackerType);

        switch (cacheManagerType.toUpperCase()) {
            case "HAZELCAST":
                throw new IllegalArgumentException();
            default:
                return new FintCacheManager<>();
        }
    }
}
