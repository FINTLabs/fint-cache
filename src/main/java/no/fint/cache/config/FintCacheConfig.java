package no.fint.cache.config;

import no.fint.cache.CacheManager;
import no.fint.cache.FintCacheManager;
import no.fint.cache.HazelcastCacheManager;
import no.fint.cache.RedisCacheManager;
import no.fint.cache.model.CacheObject;
import no.fint.cache.model.PackerFactory;
import no.fint.cache.utils.RedisCacheConnectionFactory;
import no.fint.cache.utils.RedisConnectionString;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Collectors;

@Configuration
public class FintCacheConfig {

    @Bean
    public FintCacheProperties fintCacheProperties() {
        return new FintCacheProperties();
    }

    @Bean
    public CacheManager<?> cacheManager(FintCacheProperties properties) {
        CacheObject.PACKER = PackerFactory.create(properties.getPacker());

        switch (properties.getManager().toUpperCase()) {
            case "REDIS":
                return new RedisCacheManager<>(properties
                        .getServers()
                        .stream()
                        .map(RedisConnectionString::parse)
                        .map(RedisCacheConnectionFactory::toJedisConnectionFactory)
                        .collect(Collectors.toList()));
            case "HAZELCAST":
                return new HazelcastCacheManager<>();
            default:
                return new FintCacheManager<>();
        }
    }
}
