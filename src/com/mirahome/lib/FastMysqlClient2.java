package com.mirahome.lib;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zhoubin on 2017/4/27.
 */
public class FastMysqlClient2 {

    private static HashMap<String, FastMysqlClient2> clients = new HashMap<String, FastMysqlClient2>();

    private BasicDataSource dataSource = null;
    private PreparedStatement preparedStatement = null;
    private long expiredTime = 0;

    public static synchronized FastMysqlClient2 getInstance(String host, String dbName, String username, String password) {
        try {
            String connectionString = "jdbc:mysql://" + host + ":3306/"+dbName+"?autoReconnect=true&autoReconnectForPools=true";
            String key = DigestUtils.md5Hex(connectionString + username + password);
            if(clients.get(key) == null) {
                FastMysqlClient2 client = new FastMysqlClient2(connectionString, username, password);
                clients.put(key, client);
            }
            return clients.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private FastMysqlClient2(String connectionString, String username, String password) {
        this.dataSource = new BasicDataSource();
        this.dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        this.dataSource.setUrl(connectionString);
        this.dataSource.setUsername(username);
        this.dataSource.setPassword(password);
        this.dataSource.setInitialSize(5);
    }

    public Object insert(String sql, List params) {
        try {
            this.preparedStatement = this.dataSource.getConnection().prepareStatement(sql);
            for (int i = 0; i < params.size() ; i++) {
                preparedStatement.setObject(i+1, params.get(i));
            }
            boolean isSuccess = preparedStatement.execute();
            if(isSuccess) {
                ResultSet result = preparedStatement.getGeneratedKeys();
                Object key = result.getObject(1);
                if(!result.isClosed()) result.close();
                return key;
            }else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean update(String sql, List params) {
        try {
            this.preparedStatement = this.dataSource.getConnection().prepareStatement(sql);
            for (int i = 0; i < params.size() ; i++) {
                preparedStatement.setObject(i+1, params.get(i));
            }
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String sql, List params) {
        return this.update(sql, params);
    }

    public List<HashMap> fetchTable(String sql, List params) {
        try {
            this.preparedStatement = this.dataSource.getConnection().prepareStatement(sql);
            for (int i = 0; i < params.size() ; i++) {
                preparedStatement.setObject(i+1, params.get(i));
            }
            ResultSet result = preparedStatement.executeQuery();
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
                        case Types.BLOB:
                            row.put(columnName, result.getBlob(i));
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
            //if(!result.isClosed()) result.close();
            return table;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public HashMap<String, Object> fetchRow(String sql, List params) {
        try {
            List<HashMap> table = this.fetchTable(sql, params);
            if (table == null || table.size() == 0) {
                return null;
            } else {
                return (HashMap<String, Object>) (table.get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Object> fetchColumn(String sql, List params) {
        try {
            this.preparedStatement = this.dataSource.getConnection().prepareStatement(sql);
            for (int i = 0; i < params.size() ; i++) {
                preparedStatement.setObject(i+1, params.get(i));
            }
            ResultSet result = preparedStatement.executeQuery();
            ResultSetMetaData metaData = result.getMetaData();
            List<Object> column = new LinkedList<Object>();
            while (result.next()) {
                column.add(result.getObject(1));
            }
            if(!result.isClosed()) result.close();
            return column;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object fetchCell(String sql, List params) {
        Object returnData = new Object();
        try {
            this.preparedStatement = this.dataSource.getConnection().prepareStatement(sql);
            for (int i = 0; i < params.size() ; i++) {
                preparedStatement.setObject(i+1, params.get(i));
            }
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                returnData = result.getObject(1);
            }
            if(!result.isClosed()) result.close();
            return returnData;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

}
