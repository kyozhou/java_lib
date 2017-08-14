package com.mirahome.lib;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by zhoubin on 2017/7/6.
 */
public class Utils {

    public static String getLocalIP() {

        try {
            String addr = InetAddress.getLocalHost().getHostAddress();
            return addr;
        } catch (UnknownHostException e) {
            return "";
        }
    }
}
