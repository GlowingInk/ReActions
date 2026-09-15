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

package fun.reactions;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fun.reactions.cfg.RaConfiguration;
import fun.reactions.cfg.Reloadable;
import fun.reactions.util.Utils;
import fun.reactions.util.message.Msg;
import fun.reactions.util.num.Is;
import fun.reactions.util.num.NumberUtils;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.OptionalInt;
import java.util.Properties;

public final class SQLManager implements Reloadable {
    // TODO: Ability to create h2 tables through config file like databases.yml
    // TODO: Make from scratch

    private final ReActions.Platform rea;

    private boolean enabled = false;
    private String serverAddress;
    private String port;
    private String dataBase;
    private String userName;
    private String password;
    private String codepage;

    private HikariDataSource dataSource;
    private boolean init;

    public SQLManager(@NotNull ReActions.Platform rea) {
        this.rea = rea;
    }

    @Override
    public void acceptReload(@NotNull RaConfiguration config) {
        RaConfiguration.MySQLCfg mysql = config.mysqlCfg();
        serverAddress = mysql.server();
        port = mysql.port();
        dataBase = mysql.database();
        userName = mysql.username();
        password = mysql.password();
        codepage = mysql.codepage();
    }

    @ApiStatus.Internal
    public void init() {
        if (init) {
            throw new IllegalStateException("SQLManager is already initialized");
        }
        init = true;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Msg.logOnce("mysqlinitfail", "MySQL JDBC Driver not found!");
            enabled = false;
            return;
        }

        HikariConfig config = new HikariConfig();
        config.setPoolName("ReActions-SQL");
        config.setJdbcUrl(buildJdbcUrl(serverAddress, port, dataBase));
        config.setUsername(userName);
        config.setPassword(password);
        if (!codepage.isEmpty()) {
            config.addDataSourceProperty("useUnicode", "true");
            config.addDataSourceProperty("characterEncoding", codepage);
        }
        config.setMaximumPoolSize(10);
        config.setInitializationFailTimeout(-1);

        dataSource = new HikariDataSource(config);
        enabled = true;
    }

    @ApiStatus.Internal
    public void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

    public boolean compareSelect(String value, String query, int column, Parameters params, String sqlset) {
        String result = executeSelect(query, column, params, sqlset);
        OptionalInt resultOpt = NumberUtils.parseInteger(result, Is.NON_NEGATIVE);
        if (resultOpt.isPresent()) {
            OptionalInt valueOpt = NumberUtils.parseInteger(value, Is.NON_NEGATIVE);
            if (valueOpt.isPresent()) {
                return resultOpt.getAsInt() == valueOpt.getAsInt();
            }
        }
        return result.equalsIgnoreCase(value);
    }

    private static String buildJdbcUrl(String address, String port, String dataBase) {
        return "jdbc:mysql://" + address + (port.isEmpty() ? "" : ":" + port) + "/" + dataBase;
    }

    private Connection getConnection(Parameters params) throws SQLException {
        if (!params.containsAny("server", "port", "db", "user", "password", "codepage")) {
            return dataSource.getConnection();
        }

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
        return DriverManager.getConnection(buildJdbcUrl(cAddress, cPort, cDataBase), prop);
    }

    public String executeSelect(String query, int column, Parameters params, String sqlset) {
        if (!enabled) return "";

        String resultStr = "";
        try (
                Connection connection = getConnection(params);
                Statement selectStmt = connection.createStatement()
        ) {
            if (!Utils.isStringEmpty(sqlset)) {
                selectStmt.execute(sqlset);
            }
            try (ResultSet result = selectStmt.executeQuery(query)) {
                if (result.next()) {
                    int columns = result.getMetaData().getColumnCount();
                    if (column > 0 && column <= columns) resultStr = result.getString(column);
                }
            }
        } catch (SQLException e) {
            rea.logger().error("Failed to execute SQL query: {}", query, e);
        }
        return resultStr;
    }

    public boolean executeUpdate(String query, Parameters params) {
        if (!enabled) return false;
        try (
                Connection connection = getConnection(params);
                Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(query);
            return true;
        } catch (SQLException e) {
            rea.logger().error("Failed to execute SQL query: {}", query, e);
            return false;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isSelectResultEmpty(String query) {
        if (!enabled) return false;

        boolean resultBool = false;
        try (
                Connection connection = getConnection(Parameters.fromString(""));
                Statement selectStmt = connection.createStatement();
                ResultSet result = selectStmt.executeQuery(query)
        ) {
            resultBool = result.next();
        } catch (SQLException e) {
            rea.logger().error("Failed to execute SQL query: {}", query, e);
        }
        return resultBool;
    }
}
