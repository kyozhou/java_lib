package com.mirahome.lib;

//public class FastMysqlClient2Test extends groovy.util.GroovyTestCase {
//    public FastMysqlClient2Test() {
//        this.testInsert();
//    }

//    @Test
//    public void testInsert() {
//        FastMysqlClient2 dbDefault = FastMysqlClient2.getInstance(
//                "54.222.153.45",
//                "test",
//                "root",
//                "Mianmian520");
//        boolean result = dbDefault.insertForBool(
//                "INSERT INTO ms_db(id, name)VALUES(?, ?)",
//                UUID.randomUUID().toString(), "test" + System.currentTimeMillis()
//        );
//        System.out.println("test insert end" + (result));
//    }
//}