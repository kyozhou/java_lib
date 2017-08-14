package com.mirahome.lib

/**
 * Created by zhoubin on 2017/7/6.
 */
class UtilsTest extends GroovyTestCase {
    void testGetLocalIP() {
        System.out.println("local ip : " + Utils.getLocalIP());
    }
}
