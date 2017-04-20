package no.fint.cache;

import lombok.extern.slf4j.Slf4j;
import no.fint.cache.model.CacheMetaData;
import no.fint.cache.model.CacheObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.util.DigestUtils.md5DigestAsHex;

@Slf4j
public class FintCache<T> implements Cache<T> {
    private CacheMetaData cacheMetaData;
    private List<CacheObject<T>> cacheObjectList;

    public FintCache() {
        cacheMetaData = new CacheMetaData();
        cacheObjectList = new ArrayList<>();
    }

    public void update(List<T> objects) {
        Map<String, CacheObject<T>> cacheObjectMap = getMap(objects);
        if (cacheObjectList.isEmpty()) {
            log.debug("Empty cache, adding all values");
            cacheObjectList.addAll(cacheObjectMap.values());
        } else {
            List<CacheObject<T>> cacheObjectListCopy = new ArrayList<>(cacheObjectList);
            cacheObjectList.forEach(cacheObject -> {
                String md5sum = cacheObject.getMd5Sum();
                if (cacheObjectMap.containsKey(md5sum)) {
                    cacheObjectMap.remove(md5sum);
                } else {
                    log.debug("Adding new object to the cache (md5: {})", cacheObject.getMd5Sum());
                    cacheObjectListCopy.remove(cacheObject);
                }
            });

            cacheObjectListCopy.addAll(cacheObjectMap.values());
            cacheObjectList = cacheObjectListCopy;
        }

        updateMetaData();
    }

    public void refresh(List<T> objects) {
        cacheObjectList.clear();
        update(objects);
    }

    public void flush() {
        flushMetaData();
        cacheObjectList.clear();
    }

    public List<CacheObject<T>> get() {
        return cacheObjectList;
    }

    public List<T> getSourceList() {
        return cacheObjectList.stream().map(CacheObject::getObject).collect(Collectors.toList());
    }

    public List<CacheObject<T>> getSince(long timestamp) {
        return cacheObjectList.stream().filter(cacheObject ->
                (cacheObject.getLastUpdated() >= timestamp)).collect(Collectors.toList());
    }

    public List<?> getSourceListSince(long timestamp) {
        return cacheObjectList.stream().filter(cacheObject ->
                (cacheObject.getLastUpdated() >= timestamp))
                .collect(Collectors.toList())
                .stream().map(CacheObject::getObject)
                .collect(Collectors.toList());
    }


    private void updateMetaData() {
        cacheMetaData.setCacheCount(cacheObjectList.size());
        cacheMetaData.setLastUpdated(System.currentTimeMillis());
        cacheMetaData.setMd5Sum(md5DigestAsHex(cacheObjectList.toString().getBytes()));
    }

    private Map<String, CacheObject<T>> getMap(List<T> list) {
        Map<String, CacheObject<T>> cacheObjectMap = new HashMap<>();
        list.forEach(o -> {
            CacheObject<T> cacheObject = new CacheObject<>(o);
            cacheObjectMap.put(cacheObject.getMd5Sum(), cacheObject);
        });

        return cacheObjectMap;
    }

    private void flushMetaData() {
        cacheMetaData.setCacheCount(0);
        cacheMetaData.setLastUpdated(0);
        cacheMetaData.setMd5Sum(null);
    }

    public CacheMetaData getCacheMetaData() {
        return cacheMetaData;
    }

    public long getLastUpdated() {
        return cacheMetaData.getLastUpdated();
    }
}
