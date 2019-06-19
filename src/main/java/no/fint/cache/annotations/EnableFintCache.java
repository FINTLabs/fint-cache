package no.fint.cache.annotations;

import no.fint.cache.config.FintCacheConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({FintCacheConfig.class})
public @interface EnableFintCache {
}
