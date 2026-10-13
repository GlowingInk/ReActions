package fun.reactions.variables;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fun.reactions.ReActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.*;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

final class H2VariableStorage {
    private final ReActions.Platform rea;
    private final HikariDataSource dataSource;
    private final Queue<PendingWrite> pending = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean draining = new AtomicBoolean();
    private volatile boolean closed;

    H2VariableStorage(@NotNull ReActions.Platform rea, @NotNull File dbBase) {
        this.rea = rea;
        this.dataSource = openDataSource(dbBase);
        createTable();
    }

    private @NotNull HikariDataSource openDataSource(@NotNull File dbBase) {
        HikariConfig config = new HikariConfig();
        config.setPoolName("ReActions-Variables-H2");
        config.setJdbcUrl("jdbc:h2:file:" + dbBase.getAbsolutePath() + ";AUTO_SERVER=FALSE");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(4);
        config.setInitializationFailTimeout(-1);
        return new HikariDataSource(config);
    }

    private void createTable() {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()
        ) {
            statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS variables (
                        owner VARCHAR(255) NOT NULL,
                        var VARCHAR(255) NOT NULL,
                        val CLOB NOT NULL,
                        PRIMARY KEY (owner, var)
                    )
                    """
            );
        } catch (SQLException ex) {
            rea.logger().error("Failed to initialize the H2 variables table", ex);
        }
    }

    public boolean load(@NotNull Map<String, Map<String, String>> vars) {
        flush();
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT owner, var, val FROM variables")
        ) {
            while (result.next()) {
                vars.computeIfAbsent(result.getString("owner"), o -> new ConcurrentHashMap<>())
                        .put(result.getString("var"), result.getString("val"));
            }
            return true;
        } catch (SQLException ex) {
            rea.logger().error("Failed to load variables from H2", ex);
            return false;
        }
    }

    public boolean saveAll(@NotNull Map<String, Map<String, String>> vars) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement insert = connection.prepareStatement(
                    "MERGE INTO variables (owner, var, val) KEY (owner, var) VALUES (?, ?, ?)"
            )) {
                for (Map.Entry<String, Map<String, String>> ownerEntry : vars.entrySet()) {
                    for (Map.Entry<String, String> varEntry : ownerEntry.getValue().entrySet()) {
                        insert.setString(1, ownerEntry.getKey());
                        insert.setString(2, varEntry.getKey());
                        insert.setString(3, varEntry.getValue());
                        insert.addBatch();
                    }
                }
                insert.executeBatch();
            }
            connection.commit();
            return true;
        } catch (SQLException ex) {
            rea.logger().error("Failed to save variables to H2", ex);
            return false;
        }
    }

    public void saveVariable(@NotNull String owner, @NotNull String var, @NotNull String value) {
        enqueue(new PendingWrite(owner, var, value));
    }

    public void removeVariable(@NotNull String owner, @NotNull String var) {
        enqueue(new PendingWrite(owner, var, null));
    }

    public void close() {
        closed = true;
        flush();
        dataSource.close();
    }

    private void enqueue(@NotNull PendingWrite write) {
        if (closed) {
            rea.logger().warn("Dropped write to variable {}.{} - storage is already closed", write.owner(), write.var());
            return;
        }
        pending.add(write);
        scheduleDrain();
    }

    private void scheduleDrain() {
        if (!draining.compareAndSet(false, true)) return;
        rea.getServer().getAsyncScheduler().runNow(rea.getPlugin(), task -> {
            try {
                flush();
            } finally {
                draining.set(false);
            }
            if (!pending.isEmpty() && !closed) scheduleDrain();
        });
    }

    private synchronized void flush() {
        if (pending.isEmpty()) return;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement merge = connection.prepareStatement(
                        "MERGE INTO variables (owner, var, val) KEY (owner, var) VALUES (?, ?, ?)"
                );
                PreparedStatement delete = connection.prepareStatement(
                        "DELETE FROM variables WHERE owner = ? AND var = ?"
                )
        ) {
            PendingWrite write;
            while ((write = pending.poll()) != null) {
                try {
                    if (write.value() == null) {
                        delete.setString(1, write.owner());
                        delete.setString(2, write.var());
                        delete.executeUpdate();
                    } else {
                        merge.setString(1, write.owner());
                        merge.setString(2, write.var());
                        merge.setString(3, write.value());
                        merge.executeUpdate();
                    }
                } catch (SQLException ex) {
                    rea.logger().error("Failed to write variable {}.{} to H2", write.owner(), write.var(), ex);
                }
            }
        } catch (SQLException ex) {
            rea.logger().error("Failed to open H2 connection for variable writes", ex);
        }
    }

    private record PendingWrite(@NotNull String owner, @NotNull String var, @Nullable String value) { }
}
