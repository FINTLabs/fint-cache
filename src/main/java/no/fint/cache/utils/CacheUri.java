package no.fint.cache.utils;


import com.google.common.collect.Ordering;
import lombok.extern.slf4j.Slf4j;
import no.fint.cache.CacheService;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static boolean containsOrgId(List<CacheService> cacheServices, String orgId) {
        List<String> cacheUris = getCacheUris(cacheServices);
        Optional<String> existingOrg = cacheUris.stream().filter(o -> CacheUri.containsOrgId(o, orgId)).findAny();
        return existingOrg.isPresent();
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

    public static List<String> getCacheUris(List<CacheService> cacheServices) {
        Stream<String> keyStream = cacheServices.stream().map((Function<CacheService, Set>) CacheService::getKeys).filter(Objects::nonNull).flatMap(Collection::stream);
        List<String> cacheUris = keyStream.collect(Collectors.toList());
        return Ordering.natural().sortedCopy(cacheUris);
    }
}
