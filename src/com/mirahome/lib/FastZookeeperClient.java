package com.mirahome.lib;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhoubin on 2018/10/22.
 */
public class FastZookeeperClient {

    private ZooKeeper zkClient = null;
    private static FastZookeeperClient configLoaderInstance = null;
    private boolean isConnectingZk = false;
    private HashMap<String, HashMap> config = null;

    public static FastZookeeperClient getInstance() {
        if(FastZookeeperClient.configLoaderInstance == null) {
            FastZookeeperClient.configLoaderInstance = new FastZookeeperClient();
        }
        return FastZookeeperClient.configLoaderInstance;
    }

    private FastZookeeperClient() {
        this.connectZkIfDisconnected();
    }

    private void connectZkIfDisconnected() {
        if(this.zkClient == null || !this.isConnected()) {
            try {
                this.zkClient = new ZooKeeper(System.getenv("zk"), 30000, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isConnected() {
        return this.zkClient.getState() == ZooKeeper.States.CONNECTED;
    }

    public Object load(String basePath) {
        this.connectZkIfDisconnected();
        if(this.zkClient.getState() != ZooKeeper.States.CONNECTED) {
            return null;
        }
        HashMap<String, Object> config = new HashMap<String, Object>();
        try {
            List<String> configList = this.zkClient.getChildren(basePath,true);
            if(configList.size() > 0) {
                for(int i = 0; i < configList.size(); i++) {
                    String key = configList.get(i);
                    String pathTmp = basePath + "/" + key;
                    String value = new String(this.zkClient.getData(pathTmp, false, null));
                    if(value.length() == 0) {
                        config.put(key, this.load(pathTmp));
                    } else {
                        config.put(key, value);
                    }
                }
                return config;
            }else {
                return new String(this.zkClient.getData(basePath, false, null));
            }
        } catch (KeeperException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


}
