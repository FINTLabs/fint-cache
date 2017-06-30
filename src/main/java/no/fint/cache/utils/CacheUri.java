package no.fint.cache.utils;


import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Based on rfc2141: https://www.ietf.org/rfc/rfc2141.txt
 */
@Slf4j
public class CacheUri {

    public static String create(String orgId, String model) {
        if (StringUtils.isEmpty(model)) {
            throw new IllegalArgumentException("Model must be set on cache implementation");
        }

        return String.format("urn:fint.no:%s:%s", orgId, model);
    }

    public static boolean containsOrgId(String cacheUri, String orgId) {
        if (StringUtils.isEmpty(cacheUri) || StringUtils.isEmpty(orgId)) {
            throw new IllegalArgumentException("Input value for cacheUri and orgId cannot be null");
        }

        String[] parts = cacheUri.split(":");
        if (parts.length == 4) {
            String org = parts[2];
            return (org.equals(orgId));
        } else {
            throw new IllegalArgumentException("Invalid cacheUri format");
        }
    }
}
