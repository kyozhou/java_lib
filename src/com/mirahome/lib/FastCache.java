package com.mirahome.lib;

import org.apache.commons.collections4.map.LinkedMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FastCache {
    private static HashMap<String, FastCache> instances = new HashMap<String, FastCache>();
    private static boolean isAutoCleanerStarted = false;

    private LinkedMap<String, Object> cache = new LinkedMap<>();
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
                                ((FastCache) entry.getValue()).removeCache(null);
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
        Integer timestampNow = (int) (System.currentTimeMillis() / 1000);
        HashMap<String, Object> obj2Set = new HashMap<>();
        obj2Set.put("value", value);
        obj2Set.put("time_expire", timestampNow + timeout);
        this.cache.put(key, obj2Set);
        while(this.cache.size() > 1000000) {
            this.removeCache(0);
        }
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
            this.removeCache(key);
            return null;
        }else {
            return valueOuter.get("value");
        }
    }

    private synchronized void removeCache(Object key) {
        try {
            if(key == null) {
                Integer timestampNow = (int) (System.currentTimeMillis() / 1000);
                Integer cacheSize = this.cache.size();
                for (int i = 0; i < cacheSize; i++) {
                    try {
                        Object value = this.cache.getValue(i);
                        HashMap<String, Object> valueOuter = value instanceof HashMap ?
                                (HashMap) value : null;
                        if (valueOuter == null || (valueOuter.containsKey("time_expire")
                                && (Integer) valueOuter.get("time_expire") < timestampNow)) {
                            this.cache.remove(i);
                        }
                    }catch (NullPointerException e) {
                        System.out.println("Warning: cache "+ i +" is not matched ...........");
                    }
                }
            } else {
                if(key instanceof String) {
                    if (this.cache.containsKey(key)) {
                        this.cache.remove(key);
                    }
                }else if(key instanceof Integer) {
                    int keyInt = Integer.parseInt(key.toString());
                    this.cache.remove(keyInt);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


