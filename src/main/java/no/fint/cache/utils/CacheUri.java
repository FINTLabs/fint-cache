package no.fint.cache.utils;


/**
 * Based on rfc2141: https://www.ietf.org/rfc/rfc2141.txt
 */
public class CacheUri {

    public static String create(String orgId, String model) {
        return String.format("urn:fint.no:%s:%s", orgId, model);
    }
}
