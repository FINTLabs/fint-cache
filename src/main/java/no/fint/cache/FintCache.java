package no.fint.cache;

import no.fint.cache.model.CacheMetaData;
import no.fint.cache.model.CacheObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.util.DigestUtils.md5DigestAsHex;

public class FintCache implements Cache {
    private CacheMetaData cacheMetaData;
    private List<CacheObject> cacheObjectList;

    public FintCache() {
        cacheMetaData = new CacheMetaData();
        cacheObjectList = new ArrayList<>();
    }

    public void update(Map<String, CacheObject> cacheObjectMap) {
        if (cacheObjectList.isEmpty()) {
            cacheObjectList.addAll(cacheObjectMap.values());
        } else {
            List<CacheObject> cacheObjectListCopy = new ArrayList<>(cacheObjectList);
            cacheObjectList.forEach( cacheObject -> {
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

    public void refresh(Map<String, CacheObject> cacheObjectMap) {
        cacheObjectList.clear();
        update(cacheObjectMap);
    }

    public void flush() {
        flushMetaData();
        cacheObjectList.clear();
    }

    public List<CacheObject> get() {
        return cacheObjectList;
    }

    public List<CacheObject> getSince(long timestamp) {
        return cacheObjectList.stream().filter(cacheObject ->
                (cacheObject.getLastUpdated() >= timestamp)).collect(Collectors.toList());
    }

    private void updateMetaData() {
        cacheMetaData.setCacheCount(cacheObjectList.size());
        cacheMetaData.setLastUpdated(System.currentTimeMillis());
        cacheMetaData.setMd5Sum(md5DigestAsHex(cacheObjectList.toString().getBytes()));
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
