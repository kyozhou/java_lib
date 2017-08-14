package com.mirahome.lib;

import org.apache.commons.codec.digest.DigestUtils;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zhoubin on 2017/4/27.
 */
public class FastMysqlClient {

    private static HashMap<String, FastMysqlClient> mysqls = new HashMap<String, FastMysqlClient>();
    Connection conn = null;
    Statement stmt = null;
    private String connectionString = null;
    private String username = null;
    private String password = null;

    public static FastMysqlClient getInstance(String connectionString, String username, String password) {
        String key = DigestUtils.md5Hex(connectionString);
        if(FastMysqlClient.mysqls.get(key) == null) {
            FastMysqlClient fastMysqlClient = new FastMysqlClient(connectionString, username, password);
            FastMysqlClient.mysqls.put(key, fastMysqlClient);
        }
        return FastMysqlClient.mysqls.get(key);
    }

    public FastMysqlClient(String connectionString, String username, String password) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            //this.conn = DriverManager.getConnection(connectionString + "user=mira&password=Mianmian2o16&useUnicode=true&characterEncoding=UTF-8");
            this.connectionString = connectionString;
            this.username = username;
            this.password = password;
            this.connectMysql();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean connectMysql() {
        try {
            if(this.conn == null || this.conn.isClosed()) {
                this.conn = DriverManager.getConnection(this.connectionString, this.username, this.password);
                return true;
            }else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int insert(String sql) {
        try {
            if(this.conn.isClosed()) {
                this.connectMysql();
            }
            this.stmt = conn.createStatement();
            boolean isSuccsss = this.stmt.execute(sql);
            if(isSuccsss) {
                ResultSet result = this.stmt.getGeneratedKeys();
                return result.getInt(1);
            }else {
                return 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean update(String sql) {
        try {
            if(this.conn.isClosed()) {
                this.connectMysql();
            }
            this.stmt = conn.createStatement();
            return this.stmt.executeUpdate(sql) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(String sql) {
        return this.update(sql);
    }

    public List<HashMap> fetchTable(String sql) {
        try {
            if(this.conn.isClosed()) {
                this.connectMysql();
            }
            this.stmt = conn.createStatement();
            ResultSet result = this.stmt.executeQuery(sql);
            List<HashMap> table = new LinkedList<HashMap>();
            HashMap<String, Object> row;
            ResultSetMetaData metaData = result.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (result.next()) {
                row = new HashMap<String, Object>();
                for (int i=1; i<=columnCount; i++) {
                    int columnType = metaData.getColumnType(i);
                    String columnName = metaData.getColumnName(i);
                    switch (columnType) {
                        case Types.INTEGER:
                        case Types.BIGINT:
                        case Types.SMALLINT:
                        case Types.TINYINT:
                        case Types.BIT:
                            row.put(columnName, result.getInt(i));
                            break;
                        case Types.FLOAT:
                        case Types.DECIMAL:
                            row.put(columnName, result.getFloat(i));
                            break;
                        case Types.VARCHAR:
                        case Types.NVARCHAR:
                        case Types.CHAR:
                            row.put(columnName, result.getString(i));
                            break;

                    }
                }
                table.add(row);
            }
            return table;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public HashMap<String, Object> fetchRow(String sql) {
        List<HashMap> table = this.fetchTable(sql);
        return table.size() >=1 ? table.get(0) : new HashMap<String, Object>();
    }

    public List<Object> fetchColumn(String sql) {
        try {
            if(this.conn.isClosed()) {
                this.connectMysql();
            }
            this.stmt = conn.createStatement();
            ResultSet result = this.stmt.executeQuery(sql);
            ResultSetMetaData metaData = result.getMetaData();
            List<Object> column = new LinkedList<Object>();
            while (result.next()) {
                column.add(result.getObject(1));
            }
            return column;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object fetchCell(String sql) {
        try {
            if(this.conn.isClosed()) {
                this.connectMysql();
            }
            this.stmt = conn.createStatement();
            ResultSet result = this.stmt.executeQuery(sql);
            if (result.next()) {
                return result.getObject(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Object();
    }
}
