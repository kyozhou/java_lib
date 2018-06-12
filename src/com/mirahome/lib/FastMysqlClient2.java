package com.mirahome.lib;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhoubin on 2018/6/5.
 */
public class FastMysqlClient2 {

    private static HashMap<String, FastMysqlClient2> clients = new HashMap<String, FastMysqlClient2>();

    private BasicDataSource dataSource = null;
    private Connection transConnection = null;

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

    public synchronized void offAutoCommit() {
        try {
            this.transConnection = this.dataSource.getConnection();
            this.transConnection.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                this.transConnection.setAutoCommit(true);
            } catch (SQLException eInner) {
                eInner.printStackTrace();
            }
        }
    }

    public synchronized void rollback() {
        try {
            this.transConnection.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void onAutoCommit() {
        try {
            this.transConnection.commit();
            this.transConnection.setAutoCommit(true);
            this.transConnection.close();
            this.transConnection = null;
        } catch (SQLException e) {
            this.transConnection = null;
            e.printStackTrace();
        }
    }


    public boolean execute(String sql, Object... params) {
        Integer result = this.insertOrigin(sql, false, params);
        return result != null && result > 0;
    }

    public Integer insert(String sql, Object... params) {
        Integer affectedRows = this.insertOrigin(sql, true, params);
        return affectedRows;
    }

    private Integer insertOrigin(String sql, boolean returnGeneratedIntKey, Object... params) {
        try {
            Connection connection = null;
            if(this.transConnection != null && !this.transConnection.getAutoCommit()) {
                connection = this.transConnection;
            } else {
                connection = this.dataSource.getConnection();
            }
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length ; i++) {
                preparedStatement.setObject(i+1, params[i]);
            }
            int affectedRows = preparedStatement.executeUpdate();
            if(affectedRows > 0) {
                if(returnGeneratedIntKey) {
                    ResultSet result = preparedStatement.getGeneratedKeys();
                    if (result.next()) {
                        String stringKey = result.getString(1);
                        Integer key = result.getInt(1);
                        if (!result.isClosed()) result.close();
                        if (!preparedStatement.isClosed()) preparedStatement.close();
                        if (this.transConnection == null) {
                            if (!connection.isClosed()) connection.close();
                        }
                        return key;
                    } else {
                        return 0;
                    }
                } else {
                    return affectedRows;
                }
            }else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    public boolean update(String sql, Object... params) {
        return this.updateOrigin(sql, params);
    }

    private boolean updateOrigin(String sql, Object[] params) {
        try {
            Connection connection = null;
            if(this.transConnection != null && !this.transConnection.getAutoCommit()) {
                connection = this.transConnection;
            } else {
                connection = this.dataSource.getConnection();
            }
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < params.length ; i++) {
                preparedStatement.setObject(i+1, params[i]);
            }
            int affectedRows = preparedStatement.executeUpdate();
            if(!preparedStatement.isClosed()) preparedStatement.close();
            if(this.transConnection == null) {
                if (!connection.isClosed()) connection.close();
            }
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String sql, Object... params) {
        return this.updateOrigin(sql, params);
    }

    public List<HashMap> fetchTable(String sql, Object... params) {
        return this.fetchTableOrigin(sql, params);
    }

    private List<HashMap> fetchTableOrigin(String sql, Object[] params) {
        try {
            Connection connection = this.dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < params.length ; i++) {
                preparedStatement.setObject(i+1, params[i]);
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
            if(!result.isClosed()) result.close();
            if(!preparedStatement.isClosed()) preparedStatement.close();
            if(!connection.isClosed()) connection.close();
            return table;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, Object> fetchRow(String sql, Object... params) {
        try {
            List<HashMap> table = this.fetchTableOrigin(sql, params);
            if (table == null || table.size() == 0) {
                return null;
            } else {
                return (Map<String, Object>) (table.get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Object> fetchColumn(String sql, Object... params) {
        try {
            Connection connection = this.dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < params.length ; i++) {
                preparedStatement.setObject(i+1, params[i]);
            }
            ResultSet result = preparedStatement.executeQuery();
            ResultSetMetaData metaData = result.getMetaData();
            List<Object> column = new LinkedList<Object>();
            while (result.next()) {
                column.add(result.getObject(1));
            }
            if(!result.isClosed()) result.close();
            if(!preparedStatement.isClosed()) preparedStatement.close();
            if(!connection.isClosed()) connection.close();
            return column;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object fetchCell(String sql, Object... params) {
        Object returnData = new Object();
        try {
            Connection connection = this.dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < params.length ; i++) {
                preparedStatement.setObject(i+1, params[i]);
            }
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                returnData = result.getObject(1);
            }
            if(!result.isClosed()) result.close();
            if(!preparedStatement.isClosed()) preparedStatement.close();
            if(!connection.isClosed()) connection.close();
            return returnData;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

}
