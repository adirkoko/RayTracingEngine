package metrics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import renderer.ImageWriter;
import renderer.RenderProgress;
import renderer.RenderProgressListener;
import renderer.RenderStage;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * Factory methods for optional render progress persistence listeners.
 */
public final class RenderProgressWriters {

    /**
     * Number of events to buffer before flushing progress storage.
     */
    private static final int BUFFERED_WRITE_BATCH_SIZE = 64;

    /**
     * Private constructor to prevent utility class instantiation.
     */
    private RenderProgressWriters() {
    }

    /**
     * Creates a CSV progress listener.
     *
     * @param path destination CSV file
     * @return progress listener that appends render progress rows to the file
     */
    public static RenderProgressListener csv(Path path) {
        return new CsvProgressWriter(path);
    }

    /**
     * Creates a CSV progress listener in the default images directory.
     *
     * @param imageName image name without extension
     * @return progress listener writing to {@code images/<imageName>-progress.csv}
     */
    public static RenderProgressListener csvForImage(String imageName) {
        return csv(progressPath(imageName, "csv"));
    }

    /**
     * Creates a SQLite progress listener.
     *
     * @param path destination SQLite database file
     * @return progress listener that stores render progress rows in SQLite
     */
    public static RenderProgressListener sqlite(Path path) {
        return new SqliteProgressWriter(path);
    }

    /**
     * Creates a SQLite progress listener in the default images directory.
     *
     * @param imageName image name without extension
     * @return progress listener writing to {@code images/<imageName>-progress.sqlite}
     */
    public static RenderProgressListener sqliteForImage(String imageName) {
        return sqlite(progressPath(imageName, "sqlite"));
    }

    /**
     * Resolves a progress file path in the default images directory.
     *
     * @param imageName image name without extension
     * @param extension progress file extension
     * @return progress file path
     */
    private static Path progressPath(String imageName, String extension) {
        if (imageName == null || imageName.isBlank())
            throw new IllegalArgumentException("Image name cannot be null or blank");
        return ImageWriter.getOutputDirectory().resolve(imageName + "-progress." + extension);
    }

    /**
     * Creates parent directories for the requested file path.
     *
     * @param path file path
     */
    private static void createParentDirectories(Path path) {
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create progress output directory for " + path, e);
        }
    }

    /**
     * Checks whether the render stage completes the lifecycle.
     *
     * @param stage render stage
     * @return true for terminal stages
     */
    private static boolean isTerminal(RenderStage stage) {
        return stage == RenderStage.DONE || stage == RenderStage.FAILED;
    }

    /**
     * Writes render progress events to CSV.
     */
    private static final class CsvProgressWriter implements RenderProgressListener {

        /**
         * Destination CSV file.
         */
        private final Path path;

        /**
         * Buffered writer for CSV rows.
         */
        private BufferedWriter writer;

        /**
         * Rows written since the last flush.
         */
        private int rowsSinceFlush = 0;

        /**
         * Creates a CSV progress writer.
         *
         * @param path destination CSV file
         */
        private CsvProgressWriter(Path path) {
            if (path == null)
                throw new IllegalArgumentException("CSV path cannot be null");

            this.path = path;
            createParentDirectories(path);
            try {
                open(true);
            } catch (IOException e) {
                throw new IllegalStateException("Could not create render progress CSV " + path, e);
            }
        }

        @Override
        public synchronized void onProgress(RenderProgress progress) {
            try {
                ensureOpen();
                writer.write(toCsvRow(progress));
                writer.newLine();
                if (++rowsSinceFlush >= BUFFERED_WRITE_BATCH_SIZE || isTerminal(progress.stage()))
                    flush();
                if (isTerminal(progress.stage())) close();
            } catch (IOException e) {
                throw new IllegalStateException("Could not write render progress CSV to " + path, e);
            }
        }

        @Override
        public synchronized void close() {
            if (writer == null) return;
            try {
                flush();
                writer.close();
                writer = null;
            } catch (IOException e) {
                throw new IllegalStateException("Could not close render progress CSV " + path, e);
            }
        }

        /**
         * Converts a progress event to one CSV row.
         *
         * @param progress progress event
         * @return CSV row
         */
        private static String toCsvRow(RenderProgress progress) {
            return String.join(",",
                    escape(progress.renderId()),
                    progress.stage().name(),
                    Long.toString(progress.completedWork()),
                    Long.toString(progress.totalWork()),
                    String.format(Locale.ROOT, "%.6f", progress.percent()),
                    Long.toString(progress.elapsedMillis()),
                    Long.toString(progress.stageElapsedMillis()),
                    Long.toString(progress.timestampMillis()));
        }

        /**
         * Escapes a CSV field.
         *
         * @param value field value
         * @return escaped CSV field
         */
        private static String escape(String value) {
            if (value.indexOf(',') < 0 && value.indexOf('"') < 0 && value.indexOf('\n') < 0 && value.indexOf('\r') < 0)
                return value;
            return '"' + value.replace("\"", "\"\"") + '"';
        }

        /**
         * Opens the CSV writer.
         *
         * @param truncate true to create a fresh file and write the header
         * @throws IOException if the writer cannot be opened
         */
        private void open(boolean truncate) throws IOException {
            writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, csvOpenOptions(truncate));
            rowsSinceFlush = 0;
            if (truncate) {
                writer.write(String.join(",",
                        "render_id",
                        "stage",
                        "completed_work",
                        "total_work",
                        "percent",
                        "elapsed_millis",
                        "stage_elapsed_millis",
                        "timestamp_millis"));
                writer.newLine();
                writer.flush();
            }
        }

        /**
         * Ensures the CSV writer is available.
         *
         * @throws IOException if the writer cannot be reopened
         */
        private void ensureOpen() throws IOException {
            if (writer == null) open(false);
        }

        /**
         * Flushes buffered CSV rows.
         *
         * @throws IOException if flushing fails
         */
        private void flush() throws IOException {
            writer.flush();
            rowsSinceFlush = 0;
        }

        /**
         * Selects CSV file open options.
         *
         * @param truncate true to overwrite the file
         * @return open options
         */
        private static OpenOption[] csvOpenOptions(boolean truncate) {
            return truncate
                    ? new OpenOption[]{CREATE, TRUNCATE_EXISTING}
                    : new OpenOption[]{CREATE, APPEND};
        }
    }

    /**
     * Writes render progress events to SQLite.
     */
    private static final class SqliteProgressWriter implements RenderProgressListener {

        /**
         * SQLite JDBC URL.
         */
        private final String jdbcUrl;

        /**
         * SQLite connection reused across progress events.
         */
        private Connection connection;

        /**
         * Prepared event insertion statement.
         */
        private PreparedStatement insertEventStatement;

        /**
         * Prepared render-run summary upsert statement.
         */
        private PreparedStatement upsertRunStatement;

        /**
         * Events written since the last transaction commit.
         */
        private int eventsSinceCommit = 0;

        /**
         * Creates a SQLite progress writer.
         *
         * @param path destination database file
         */
        private SqliteProgressWriter(Path path) {
            if (path == null)
                throw new IllegalArgumentException("SQLite path cannot be null");

            createParentDirectories(path);
            jdbcUrl = "jdbc:sqlite:" + path.toAbsolutePath();
            initializeDatabase();
            openConnection();
        }

        @Override
        public synchronized void onProgress(RenderProgress progress) {
            try {
                ensureOpen();
                insertProgressEvent(progress);
                upsertRenderRun(progress);
                if (++eventsSinceCommit >= BUFFERED_WRITE_BATCH_SIZE || isTerminal(progress.stage()))
                    commit();
                if (isTerminal(progress.stage())) close();
            } catch (SQLException e) {
                rollback();
                throw new IllegalStateException("Could not write render progress SQLite data", e);
            }
        }

        @Override
        public synchronized void close() {
            if (connection == null) return;
            SQLException failure = null;

            try {
                commit();
            } catch (SQLException e) {
                failure = e;
            }

            failure = close(insertEventStatement, failure);
            failure = close(upsertRunStatement, failure);
            failure = close(connection, failure);
            insertEventStatement = null;
            upsertRunStatement = null;
            connection = null;
            eventsSinceCommit = 0;

            if (failure != null)
                throw new IllegalStateException("Could not close render progress SQLite data", failure);
        }

        /**
         * Creates the SQLite schema if it does not exist.
         */
        private void initializeDatabase() {
            try (Connection connection = DriverManager.getConnection(jdbcUrl);
                 Statement statement = connection.createStatement()) {
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS render_runs (
                            render_id TEXT PRIMARY KEY,
                            started_at_millis INTEGER NOT NULL,
                            finished_at_millis INTEGER,
                            status TEXT NOT NULL,
                            last_stage TEXT NOT NULL,
                            last_percent REAL NOT NULL,
                            elapsed_millis INTEGER NOT NULL
                        )
                        """);
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS render_progress_events (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            render_id TEXT NOT NULL,
                            stage TEXT NOT NULL,
                            completed_work INTEGER NOT NULL,
                            total_work INTEGER NOT NULL,
                            percent REAL NOT NULL,
                            elapsed_millis INTEGER NOT NULL,
                            stage_elapsed_millis INTEGER NOT NULL,
                            timestamp_millis INTEGER NOT NULL
                        )
                        """);
                statement.executeUpdate("""
                        CREATE INDEX IF NOT EXISTS idx_render_progress_events_render_id
                        ON render_progress_events(render_id)
                        """);
            } catch (SQLException e) {
                throw new IllegalStateException("Could not initialize render progress SQLite database", e);
            }
        }

        /**
         * Opens the SQLite connection and prepares reusable statements.
         */
        private void openConnection() {
            try {
                connection = DriverManager.getConnection(jdbcUrl);
                connection.setAutoCommit(false);
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("PRAGMA busy_timeout = 5000");
                }
                insertEventStatement = connection.prepareStatement("""
                        INSERT INTO render_progress_events (
                            render_id,
                            stage,
                            completed_work,
                            total_work,
                            percent,
                            elapsed_millis,
                            stage_elapsed_millis,
                            timestamp_millis
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """);
                upsertRunStatement = connection.prepareStatement("""
                        INSERT INTO render_runs (
                            render_id,
                            started_at_millis,
                            finished_at_millis,
                            status,
                            last_stage,
                            last_percent,
                            elapsed_millis
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        ON CONFLICT(render_id) DO UPDATE SET
                            finished_at_millis = excluded.finished_at_millis,
                            status = excluded.status,
                            last_stage = excluded.last_stage,
                            last_percent = excluded.last_percent,
                            elapsed_millis = excluded.elapsed_millis
                        """);
            } catch (SQLException e) {
                throw new IllegalStateException("Could not open render progress SQLite database", e);
            }
        }

        /**
         * Ensures the SQLite connection is available.
         *
         * @throws SQLException if connection status cannot be checked
         */
        private void ensureOpen() throws SQLException {
            if (connection == null || connection.isClosed()) openConnection();
        }

        /**
         * Inserts one progress event.
         *
         * @param progress progress event
         * @throws SQLException if the insert fails
         */
        private void insertProgressEvent(RenderProgress progress) throws SQLException {
            insertEventStatement.setString(1, progress.renderId());
            insertEventStatement.setString(2, progress.stage().name());
            insertEventStatement.setLong(3, progress.completedWork());
            insertEventStatement.setLong(4, progress.totalWork());
            insertEventStatement.setDouble(5, progress.percent());
            insertEventStatement.setLong(6, progress.elapsedMillis());
            insertEventStatement.setLong(7, progress.stageElapsedMillis());
            insertEventStatement.setLong(8, progress.timestampMillis());
            insertEventStatement.executeUpdate();
        }

        /**
         * Inserts or updates the render-run summary row.
         *
         * @param progress progress event
         * @throws SQLException if the upsert fails
         */
        private void upsertRenderRun(RenderProgress progress) throws SQLException {
            upsertRunStatement.setString(1, progress.renderId());
            upsertRunStatement.setLong(2, progress.timestampMillis() - progress.elapsedMillis());
            if (isTerminal(progress.stage()))
                upsertRunStatement.setLong(3, progress.timestampMillis());
            else
                upsertRunStatement.setNull(3, java.sql.Types.INTEGER);
            upsertRunStatement.setString(4, status(progress.stage()));
            upsertRunStatement.setString(5, progress.stage().name());
            upsertRunStatement.setDouble(6, progress.percent());
            upsertRunStatement.setLong(7, progress.elapsedMillis());
            upsertRunStatement.executeUpdate();
        }

        /**
         * Commits pending SQLite writes.
         *
         * @throws SQLException if commit fails
         */
        private void commit() throws SQLException {
            if (connection != null && eventsSinceCommit > 0) {
                connection.commit();
                eventsSinceCommit = 0;
            }
        }

        /**
         * Rolls back pending SQLite writes.
         */
        private void rollback() {
            if (connection == null) return;
            try {
                connection.rollback();
                eventsSinceCommit = 0;
            } catch (SQLException ignore) {
            }
        }

        /**
         * Closes an SQL resource and accumulates failures.
         *
         * @param resource SQL resource
         * @param failure  previous failure
         * @return accumulated failure
         */
        private static SQLException close(AutoCloseable resource, SQLException failure) {
            if (resource == null) return failure;
            try {
                resource.close();
            } catch (Exception e) {
                SQLException sqlException = e instanceof SQLException
                        ? (SQLException) e
                        : new SQLException(e);
                if (failure == null)
                    return sqlException;
                failure.addSuppressed(sqlException);
            }
            return failure;
        }

        /**
         * Converts a render stage to a run status.
         *
         * @param stage render stage
         * @return run status
         */
        private static String status(RenderStage stage) {
            return switch (stage) {
                case DONE -> "DONE";
                case FAILED -> "FAILED";
                default -> "RUNNING";
            };
        }
    }
}
