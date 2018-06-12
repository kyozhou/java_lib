package com.mirahome.lib;

import org.junit.Test;

import java.util.Arrays;


public class FastMysqlClient2Test extends groovy.util.GroovyTestCase {
//    public FastMysqlClient2Test() {
//        this.testInsert();
//    }

    @Test
    public void testInsert() {
        FastMysqlClient2 dbDefault = FastMysqlClient2.getInstance(
                "54.222.153.45",
                "test",
                "root",
                "Mianmian520");
        dbDefault.insert("INSERT INTO ms_db(name)VALUES(?)", Arrays.asList("test"));
    }
}