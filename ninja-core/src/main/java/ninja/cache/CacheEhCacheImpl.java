package ninja.cache;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;

import com.google.inject.Singleton;

/**
 * EhCache implementation.
 *
 * <p>Ehcache is an open source, standards-based cache used to boost performance,
 * offload the database and simplify scalability. Ehcache is robust, proven and
 * full-featured and this has made it the most widely-used Java-based cache.</p>
 *
 * @see http://ehcache.org/
 *
 * expiration is specified in seconds
 */
@Singleton
public class CacheEhCacheImpl implements Cache {

    private final CacheManager ehCacheManager;

    private final net.sf.ehcache.Cache ehCache;

    private static final String cacheName = "ninja";

    private Logger logger;

    @Inject
    private CacheEhCacheImpl(Logger logger) {
        this.logger = logger;
        this.ehCacheManager = CacheManager.create();
        this.ehCacheManager.addCache(cacheName);
        this.ehCache = ehCacheManager.getCache(cacheName);
    }

    public void add(String key, Object value, int expiration) {
        if (ehCache.get(key) != null) {
            return;
        }
        Element element = new Element(key, value);
        element.setTimeToLive(expiration);
        ehCache.put(element);
    }

    public void clear() {
        ehCache.removeAll();
    }

    public synchronized long decr(String key, int by) {
        Element e = ehCache.get(key);
        if (e == null) {
            return -1;
        }
        long newValue = ((Number) e.getObjectValue()).longValue() - by;
        Element newE = new Element(key, newValue);
        newE.setTimeToLive(e.getTimeToLive());
        ehCache.put(newE);
        return newValue;
    }

    public void delete(String key) {
        ehCache.remove(key);
    }

    public Object get(String key) {
        Element e = ehCache.get(key);
        return (e == null) ? null : e.getObjectValue();
    }

    public Map<String, Object> get(String[] keys) {
        Map<String, Object> result = new HashMap<String, Object>(keys.length);
        for (String key : keys) {
            result.put(key, get(key));
        }
        return result;
    }

    public synchronized long incr(String key, int by) {
        Element e = ehCache.get(key);
        if (e == null) {
            return -1;
        }
        long newValue = ((Number) e.getObjectValue()).longValue() + by;
        Element newE = new Element(key, newValue);
        newE.setTimeToLive(e.getTimeToLive());
        ehCache.put(newE);
        return newValue;

    }

    public void replace(String key, Object value, int expiration) {
        if (ehCache.get(key) == null) {
            return;
        }
        Element element = new Element(key, value);
        element.setTimeToLive(expiration);
        ehCache.put(element);
    }

    public boolean safeAdd(String key, Object value, int expiration) {
        try {
            add(key, value, expiration);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean safeDelete(String key) {
        try {
            delete(key);
            return true;
        } catch (Exception e) {
            logger.error(e.toString(), e);
            return false;
        }
    }

    public boolean safeReplace(String key, Object value, int expiration) {
        try {
            replace(key, value, expiration);
            return true;
        } catch (Exception e) {
            logger.error(e.toString(), e);
            return false;
        }
    }

    public boolean safeSet(String key, Object value, int expiration) {
        try {
            set(key, value, expiration);
            return true;
        } catch (Exception e) {
            logger.error(e.toString(), e);
            return false;
        }
    }

    public void set(String key, Object value, int expiration) {
        Element element = new Element(key, value);
        element.setTimeToLive(expiration);
        ehCache.put(element);
    }

    public void stop() {
        ehCacheManager.shutdown();
    }
}
