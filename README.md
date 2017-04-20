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

compile('no.fint:fint-cache:0.0.3')
```

## Usage

Create and update cache.

```java
List<TestDto> data = Lists.newArrayList(new TestDto());
FintCache<TestDto> cache = new FintCache<>();
cache.update(data);
```

Get values from cache.

```java
List<TestDto> cachedList = cache.getSourceList();
```

To add more logging, enable debug log level for FintCache.

```properties
logging.level.no.fint.cache=DEBUG
```