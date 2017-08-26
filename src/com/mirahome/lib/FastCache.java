package com.mirahome.lib;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FastCache {
    private static HashMap<String, FastCache> instances = new HashMap<String, FastCache>();
    private static boolean isAutoCleanerStarted = false;

    private HashMap<String, Object> cache = new HashMap<>();
    private String channel = null;


    public static FastCache getInstance(String channel) {
        if(FastCache.isAutoCleanerStarted == false) {
            FastCache.isAutoCleanerStarted = true;
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            Iterator it = FastCache.instances.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry entry = (Map.Entry) it.next();
                                ((FastCache) entry.getValue()).cleanExpired();
                            }
                            Thread.sleep(30000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            executorService.shutdown();
        }
        if(!FastCache.instances.containsKey(channel)) {
            FastCache.instances.put(channel, new FastCache(channel));
        }
        return FastCache.instances.get(channel);
    }

    private FastCache(String channel) {
        this.channel = channel;
    }

    public void set(String key, Object value) {
        this.set(key, value, 3600);
    }

    public void set(String key, Object value, Integer timeout) {
        if(this.cache.size() >= 1000000) {
            this.cache.clear();
        }
        Integer timestampNow = (int) (System.currentTimeMillis() / 1000);
        HashMap<String, Object> obj2Set = new HashMap<>();
        obj2Set.put("value", value);
        obj2Set.put("time_expire", timestampNow + timeout);
        this.cache.put(key, obj2Set);
    }

    public Integer getInt(String key) {
        return (Integer) this.get(key);
    }

    public String getString(String key) {
        return (String) this.get(key);
    }

    public HashMap getHashMap(String key) {
        return (HashMap) this.get(key);
    }

    public Object get(String key) {
        Integer timestampNow = (int) (System.currentTimeMillis() / 1000);
        HashMap valueOuter = (HashMap) this.cache.get(key);
        if(valueOuter == null || (Integer)valueOuter.get("time_expire") < timestampNow) {
            this.cache.remove(key);
            return null;
        }else {
            return valueOuter.get("value");
        }
    }

    private void cleanExpired() {
        Integer timestampNow = (int) (System.currentTimeMillis() / 1000);
        Iterator it = this.cache.entrySet().iterator();
        HashMap<String, Object> cacheTemp = new HashMap<>();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            HashMap<String, Object> valueOuter = value instanceof HashMap ? (HashMap) entry.getValue() : null;
            if(valueOuter != null && valueOuter.containsKey("time_expire")
                    && (Integer)valueOuter.get("time_expire") > timestampNow) {
                cacheTemp.put(key, valueOuter);
            }
        }
        this.cache = cacheTemp;
    }
}


