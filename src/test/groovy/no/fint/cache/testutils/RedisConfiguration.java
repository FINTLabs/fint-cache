package no.fint.cache.testutils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.util.Collections;
import java.util.List;

@Configuration
public class RedisConfiguration {

    @Bean
    public List<RedisConnectionFactory> redisConnectionFactories() {
        final JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
        connectionFactory.setHostName("localhost");
        connectionFactory.setPort(6379);
        connectionFactory.afterPropertiesSet();
        return Collections.singletonList(connectionFactory);
    }
}
