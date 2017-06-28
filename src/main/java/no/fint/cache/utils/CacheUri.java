package no.fint.cache.utils;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Based on rfc2141: https://www.ietf.org/rfc/rfc2141.txt
 */
@Slf4j
public class CacheUri {

    @Getter
    private static final Set<String> cacheUris = new HashSet<>();

    public static String create(String orgId, String model) {
        if (StringUtils.isEmpty(model)) {
            throw new IllegalArgumentException("Model must be set on cache implementation");
        }

        String cacheUri = String.format("urn:fint.no:%s:%s", orgId, model);
        cacheUris.add(cacheUri);
        return cacheUri;
    }
}
