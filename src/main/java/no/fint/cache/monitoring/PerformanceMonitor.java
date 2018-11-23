package no.fint.cache.monitoring;

import com.google.common.base.Stopwatch;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
class PerformanceMonitor {

    @Getter
    private final ConcurrentHashMap<String, Measurement> measurements = new ConcurrentHashMap<>();

    @Around("execution(* no.fint.cache.CacheService+.*(..))")
    public Object monitor(ProceedingJoinPoint joinPoint) throws Throwable {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            return joinPoint.proceed();
        } finally {
            measurements
                    .computeIfAbsent(getKey(joinPoint), k -> new Measurement())
                    .add(stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    private String getKey(ProceedingJoinPoint proceedingJoinPoint) {
        String className = proceedingJoinPoint.getTarget().getClass().getName();
        String methodName = proceedingJoinPoint.getSignature().getName();
        return String.format("%s.%s", className, methodName);
    }

    public void clearMeasurements() {
        measurements.clear();
    }

}
