# FINT Cache

[![Build Status](https://travis-ci.org/FINTlibs/fint-cache.svg?branch=master)](https://travis-ci.org/FINTlibs/fint-cache) 
[![Coverage Status](https://coveralls.io/repos/github/FINTlibs/fint-cache/badge.svg?branch=master)](https://coveralls.io/github/FINTlibs/fint-cache?branch=master)

## Installation
```groovy
repositories {
    maven {
        url  "http://dl.bintray.com/fint/maven" 
    }
}

compile('no.fint:fint-cache:1.4.0')
```

## Usage

Create and update cache:
```java
List<TestDto> data = Lists.newArrayList(new TestDto());
FintCache<TestDto> cache = new FintCache<>();
cache.update(data);
```

Get values from cache:
```java
List<TestDto> cachedList = cache.getSourceList();
```

Integration with Spring bean:
```java
@Component
public class MyCacheService extends CacheService<String> {
    
    public static final String MODEL = "test";
    
    public TestCacheService() {
        super(MODEL, MyActions.GET_ALL);
    }
    
    @PostConstruct
    public void init() {
        ...
    }
    
    @PreDestroy
    public void cleanUp() {
        ...
    }
    
    @Override
    public void onAction(Event event) {
        ...
    }
}
```

Override the `onAction()` method in `CacheService` to handle the events.


To add more logging, enable debug log level for FintCache.

```properties
logging.level.no.fint.cache=DEBUG
```


## Performance monitoring

Performance monitoring for the implementations of `CacheService`.  
It is disabled by default and can be enabled by setting the following property: `fint.cache.performance-monitor=true`

The output from the performance monitoring will be available at these endpoints:  
* `GET /performance-monitor/keys` - Lists all keys that are included in the performance monitoring
* `GET /performance-monitor/measurements?keys=<key-name>` - Lists the measurements. If a key query param is provided, it will only return the measurements containing that text
* `DELETE `/performance-monitor/measurements` - Deletes all measurements