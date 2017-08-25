package com.mirahome.lib

class UtilsTest extends groovy.util.GroovyTestCase {
    void testMd5() {

    }

    void testSerialize() {
    }

    void testDeserialize() {
    }

    void testGetLocalIP() {
    }

    void testHttpGet() {
        String returnData = Utils.httpGet("http://api.openweathermap.org/data/2.5/weather?q=&lat=en&lon=shanghai&lang=&units=&appid=b653c295f16cfa5b2e9af384bbaf4479", null);
        System.out.println(returnData);
        if(returnData.isEmpty()) {

        }
    }
}
