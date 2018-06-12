package com.mirahome.lib;

import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;


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
        boolean result = dbDefault.insert(
                "INSERT INTO ms_db(id, name)VALUES(?, ?)",
                Arrays.asList(UUID.randomUUID().toString(), "test")
        );
        System.out.println("test insert end" + (result));
    }
}