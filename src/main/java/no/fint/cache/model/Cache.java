package no.fint.cache.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.util.DigestUtils.md5DigestAsHex;

public class Cache {
    private CacheMetaData cacheMetaData;
    private List<CacheObject> cacheObjectList;

    public Cache() {
        cacheMetaData = new CacheMetaData();
        cacheObjectList = new ArrayList<>();
    }

    public void update(Map<String, CacheObject> cacheObjectMap) {

        if (cacheObjectList.isEmpty()) {
            cacheObjectList.addAll(cacheObjectMap.values());
        }
        else {
            cacheObjectList.stream().forEach(cacheObject -> {
                String md5sum = cacheObject.getMd5Sum();
                if (cacheObjectMap.containsKey(md5sum)) {
                    cacheObjectMap.remove(md5sum);
                }
                else {
                    // Remove current object
                }
            });
            cacheObjectList.addAll(cacheObjectMap.values());
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

    public List<CacheObject> getSince(Date timestamp) {
        return cacheObjectList.stream().filter(cacheObject -> {
            return ((cacheObject.getLastUpdated().after(timestamp)) || cacheObject.getLastUpdated().equals(timestamp));
        }).collect(Collectors.toList());
    }

    private void updateMetaData() {
        cacheMetaData.setCacheCount(cacheObjectList.size());
        cacheMetaData.setLastUpdated(new Date());
        cacheMetaData.setMd5Sum(md5DigestAsHex(cacheObjectList.toString().getBytes()));
    }

    private void flushMetaData() {
        cacheMetaData.setCacheCount(0);
        cacheMetaData.setLastUpdated(null);
        cacheMetaData.setMd5Sum(null);
    }


}
