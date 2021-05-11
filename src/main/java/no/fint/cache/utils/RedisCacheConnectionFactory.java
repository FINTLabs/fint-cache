package no.fint.cache.utils;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

public class RedisCacheConnectionFactory {

    public static JedisConnectionFactory toJedisConnectionFactory(RedisConnectionString connectionString) {
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
        connectionFactory.setHostName(connectionString.getHostname());
        connectionFactory.setPort(connectionString.getPort());
        if (connectionString.containsKey("password")) {
            connectionFactory.setPassword(connectionString.getProperty("password"));
        }
        if (connectionString.containsKey("ssl")) {
            connectionFactory.setUseSsl(Boolean.parseBoolean(connectionString.getProperty("ssl")));
        }
        connectionFactory.afterPropertiesSet();
        return connectionFactory;
    }
}
