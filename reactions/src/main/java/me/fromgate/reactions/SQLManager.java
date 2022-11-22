/*
 *  ReActions, Minecraft bukkit plugin
 *  (c)2012-2017, fromgate, fromgate@gmail.com
 *  http://dev.bukkit.org/server-mods/reactions/
 *
 *  This file is part of ReActions.
 *
 *  ReActions is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ReActions is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ReActions.  If not, see <http://www.gnorg/licenses/>.
 *
 */

package me.fromgate.reactions;

import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.parameter.Parameters;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public final class SQLManager {
    // TODO: Ability to create h2/sqlite tables through config file like databases.yml
    // TODO: Make from scratch
    // TODO: HikariCP

    private static boolean enabled = false;
    private static String serverAddress;
    private static String port;
    private static String dataBase;
    private static String userName;
    private static String password;
    private static String codepage;

    private SQLManager() {throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");}

    public static void init() {
        loadCfg();
        saveCfg();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            enabled = true;
        } catch (ClassNotFoundException e) {
            Msg.logOnce("mysqlinitfail", "MySQL JDBC Driver not found!");
            enabled = false;
        }
    }

    public static void loadCfg() {
        serverAddress = ReActions.getPlugin().getConfig().getString("MySQL.server", "localhost");
        port = ReActions.getPlugin().getConfig().getString("MySQL.port", "3306");
        dataBase = ReActions.getPlugin().getConfig().getString("MySQL.database", "ReActions");
        userName = ReActions.getPlugin().getConfig().getString("MySQL.username", "root");
        password = ReActions.getPlugin().getConfig().getString("MySQL.password", "password");
        codepage = ReActions.getPlugin().getConfig().getString("MySQL.codepage", "UTF-8");
    }

    public static void saveCfg() {
        ReActions.getPlugin().getConfig().set("MySQL.server", serverAddress);
        ReActions.getPlugin().getConfig().set("MySQL.port", port);
        ReActions.getPlugin().getConfig().set("MySQL.database", dataBase);
        ReActions.getPlugin().getConfig().set("MySQL.username", userName);
        ReActions.getPlugin().getConfig().set("MySQL.password", password);
        ReActions.getPlugin().getConfig().set("MySQL.codepage", codepage);
        ReActions.getPlugin().saveConfig();
    }

    public static boolean compareSelect(String value, String query, int column, Parameters params, String sqlset) {
        String result = executeSelect(query, column, params, sqlset);
        if (NumberUtils.isPositiveInt(result, value)) return (Integer.parseInt(result) == Integer.parseInt(value));
        return result.equalsIgnoreCase(value);
    }

    private static Connection connectToMySQL() {
        return connectToMySQL(Parameters.fromString(""));
    }

    // server port db user password codepage
    private static Connection connectToMySQL(Parameters params) {
        String cAddress = params.getString("server", serverAddress);
        String cPort = params.getString("port", port);
        String cDataBase = params.getString("db", dataBase);
        String cUser = params.getString("user", userName);
        String cPassword = params.getString("password", password);
        String cCodepage = params.getString("codepage", codepage);
        Properties prop = new Properties();
        if (!cCodepage.isEmpty()) {
            prop.setProperty("useUnicode", "true");
            prop.setProperty("characterEncoding", cCodepage);
        }
        prop.setProperty("user", cUser);
        prop.setProperty("password", cPassword);
        Connection connection = null;
        String connectionLine = "jdbc:mysql://" + cAddress + (cPort.isEmpty() ? "" : ":" + cPort) + "/" + cDataBase;
        try {
            connection = DriverManager.getConnection(connectionLine, prop);
        } catch (SQLException e) {
            Msg.logOnce("sqlconnect", "Failed to connect to database: " + connectionLine + " user: " + userName);
        }
        return connection;
    }


    public static String executeSelect(String query, int column, Parameters params, String sqlset) {
        if (!enabled) return "";

        Statement selectStmt = null;
        ResultSet result = null;
        String resultStr = "";
        Connection connection = connectToMySQL(params);

        try {
            selectStmt = connection.createStatement();
            if (!Utils.isStringEmpty(sqlset)) {
                selectStmt.execute(sqlset);
            }
            result = selectStmt.executeQuery(query);
            if (result.first()) {
                int columns = result.getMetaData().getColumnCount();
                if (column > 0 && column <= columns) resultStr = result.getString(column);
            }
        } catch (SQLException e) {
            Msg.logOnce(query, "Failed to execute query: " + query);
        }
        try {
            if (result != null) result.close();
            if (selectStmt != null) selectStmt.close();
            if (connection != null) connection.close();
        } catch (SQLException ignored) {
        }
        return resultStr;
    }


    public static boolean executeUpdate(String query, Parameters params) {
        if (!enabled) return false;
        Connection connection = connectToMySQL(params);
        if (connection == null) return false;
        Statement statement = null;
        boolean ok = false;
        try {
            statement = connection.createStatement();
            //statement.execute("SET NAMES 'utf8'");
            statement.executeUpdate(query);
            ok = true;
        } catch (SQLException e) {
            Msg.logOnce(query, "Failed to execute query: " + query);
            if (e.getMessage() != null) Msg.logOnce(query + e.getMessage(), e.getMessage());
            e.printStackTrace();
        }
        try {
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        } catch (SQLException ignored) {
        }
        return ok;
    }


    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isSelectResultEmpty(String query) {
        if (!enabled) return false;
        Connection connection = connectToMySQL();
        if (connection == null) return false;

        Statement selectStmt = null;
        ResultSet result = null;
        boolean resultBool = false;

        try {
            selectStmt = connection.createStatement();
            result = selectStmt.executeQuery(query);
            resultBool = result.next();
        } catch (SQLException e) {
            Msg.logOnce(query, "Failed to execute query: " + query);
            if (e.getMessage() != null) Msg.logOnce(query + e.getMessage(), e.getMessage());
        }
        try {
            if (result != null) result.close();
            if (selectStmt != null) selectStmt.close();
            if (connection != null) connection.close();
        } catch (SQLException ignored) {
        }
        return resultBool;
    }
}
