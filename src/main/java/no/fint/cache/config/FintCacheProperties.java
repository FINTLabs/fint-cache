package no.fint.cache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix="fint.cache")
@Component
@Data
public class FintCacheProperties {
    private String manager;
    private String packer;
    private List<String> servers;
}
