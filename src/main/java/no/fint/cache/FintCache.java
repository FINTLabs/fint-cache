package no.fint.cache;

import no.fint.cache.model.CacheMetaData;
import no.fint.cache.model.CacheObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.util.DigestUtils.md5DigestAsHex;

public class FintCache implements Cache {

    private CacheMetaData cacheMetaData;
    private List<CacheObject<?>> cacheObjectList;

    public FintCache() {
        cacheMetaData = new CacheMetaData();
        cacheObjectList = new ArrayList<>();
    }

    public void update(List<?> objects) {
        Map<String, CacheObject<?>> cacheObjectMap = getMap(objects);

        if (cacheObjectList.isEmpty()) {
            cacheObjectList.addAll(cacheObjectMap.values());
        } else {
            List<CacheObject<?>> cacheObjectListCopy = new ArrayList<>(cacheObjectList);
            cacheObjectList.forEach(cacheObject -> {
                String md5sum = cacheObject.getMd5Sum();
                if (cacheObjectMap.containsKey(md5sum)) {
                    cacheObjectMap.remove(md5sum);
                } else {
                    cacheObjectListCopy.remove(cacheObject);
                }
            });

            cacheObjectListCopy.addAll(cacheObjectMap.values());
            cacheObjectList = cacheObjectListCopy;
        }

        updateMetaData();
    }

    public void refresh(List<?> objects) {
        cacheObjectList.clear();
        update(objects);
    }

    public void flush() {
        flushMetaData();
        cacheObjectList.clear();
    }

    public List<CacheObject<?>> get() {
        return cacheObjectList;
    }

    public List<?> getSourceList() {
        return cacheObjectList.stream().map(CacheObject::getObject).collect(Collectors.toList());
    }

    public List<CacheObject<?>> getSince(long timestamp) {
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

    private Map<String, CacheObject<?>> getMap(List<?> list) {
        Map<String, CacheObject<?>> cacheObjectMap = new HashMap<>();

        list.forEach(o -> {
            CacheObject<?> cacheObject = new CacheObject<>(o);
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
