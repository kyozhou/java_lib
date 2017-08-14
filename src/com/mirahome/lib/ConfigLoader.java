package com.mirahome.lib;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zhoubin on 2017/4/26.
 */
public class ConfigLoader {

    private ZooKeeper zkClient = null;
    private static ConfigLoader configLoaderInstance = null;
    private boolean isConnectingZk = false;
    private HashMap<String, HashMap> config = null;

    public static ConfigLoader getInstance() {
        if(ConfigLoader.configLoaderInstance == null) {
            ConfigLoader.configLoaderInstance = new ConfigLoader();
        }
        return ConfigLoader.configLoaderInstance;
    }

    public ConfigLoader() {
        this.connectZk();
    }

    protected void connectZk() {
        if(this.zkClient != null) {
            try {
                this.zkClient.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        boolean needRetry = true;
        while (needRetry) {
            try {
                CountDownLatch connectedLatch = new CountDownLatch(1);
                Watcher watcher = new ConnectedWatcher(connectedLatch);
                this.zkClient = new ZooKeeper(System.getenv("zk"), 30000, watcher);
                System.out.println("new Zookeeper ... ");
                if (ZooKeeper.States.CONNECTING == this.zkClient.getState()) {
                    try {
                        connectedLatch.await();
                        needRetry = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
            if(needRetry) {
                System.out.println("connect zk not success, it will try after 3s ...");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Object load(String basePath) {
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

    static class ConnectedWatcher implements Watcher {

        private CountDownLatch connectedLatch;

        ConnectedWatcher(CountDownLatch connectedLatch) {
            this.connectedLatch = connectedLatch;
        }

        @Override
        public void process(WatchedEvent event) {
            if (event.getState() == Event.KeeperState.SyncConnected) {
                connectedLatch.countDown();
            }else if( event.getState() == Event.KeeperState.Expired ||event.getState() == Event.KeeperState.Disconnected) {
                System.out.println("zk connect expired or disconnected : " + event.toString());
                ConfigLoader.getInstance().connectZk();
            }
        }
    }

}
