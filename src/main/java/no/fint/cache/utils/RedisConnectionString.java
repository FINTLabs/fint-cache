package no.fint.cache.utils;

import com.google.common.net.HostAndPort;
import lombok.Data;
import lombok.experimental.Delegate;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.split;

/**
 * Represents a Redis connection.
 */
@Data
public class RedisConnectionString {
    /**
     * Parses Redis connection strings like:
     * fint-cache-beta.redis.cache.windows.net:6380,password=uVPUAlaI58NIwPrAwbxo398PdJAiAvCaA3Gjeg1JY+c=,ssl=True,abortConnect=False
     */
    public static RedisConnectionString parse(String connectionString) {
        if (StringUtils.contains(connectionString, ',')) {
            final String[] split = split(connectionString, ',');
            HostAndPort hostAndPort = HostAndPort.fromString(split[0]).requireBracketsForIPv6().withDefaultPort(6379);
            final RedisConnectionString result = new RedisConnectionString(hostAndPort.getHost(), hostAndPort.getPort());
            for (int i = 1; i < split.length; i++) {
                final String[] kv = split(split[i], "=", 2);
                result.setProperty(kv[0], kv[1]);
            }
            return result;
        } else {
            HostAndPort hostAndPort = HostAndPort.fromString(connectionString).requireBracketsForIPv6().withDefaultPort(6379);
            return new RedisConnectionString(hostAndPort.getHost(), hostAndPort.getPort());
        }
    }

    private final String hostname;
    private final int port;

    @Delegate
    private final Properties properties = new Properties();
}
