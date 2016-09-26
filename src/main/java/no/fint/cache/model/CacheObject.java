package no.fint.cache.model;

import java.util.Date;
import static org.springframework.util.DigestUtils.md5DigestAsHex;

public class CacheObject extends Object {
    private String md5Sum;
    private Date lastUpdated;
    private Object object;

    public CacheObject(Object object) {
        this.object = object;
        this.lastUpdated = new Date();
        md5Sum = md5DigestAsHex(object.toString().getBytes());
    }

    public String getMd5Sum() {
        return md5Sum;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public Object getObject() {
        return object;
    }

}
