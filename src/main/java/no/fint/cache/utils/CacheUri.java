package no.fint.cache.utils;


import org.springframework.util.StringUtils;

/**
 * Based on rfc2141: https://www.ietf.org/rfc/rfc2141.txt
 */
public class CacheUri {

    public static String create(String orgId, String model) {
        if (StringUtils.isEmpty(model)) {
            throw new IllegalArgumentException("Model must be set on cache implementation");
        }

        return String.format("urn:fint.no:%s:%s", orgId, model);
    }
}
