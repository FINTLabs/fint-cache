package no.fint.cache.testutils;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastInstanceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfiguration {

    @Bean
    public HazelcastInstance testInstance() {
        return new HazelcastInstanceFactory(new Config().setProperty("hazelcast.logging.type", "slf4j")).getHazelcastInstance();
    }
}
