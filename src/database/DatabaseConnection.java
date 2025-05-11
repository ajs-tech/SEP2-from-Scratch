package database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import util.Message;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Singleton klasse til at håndtere database forbindelser med connection pool.
 * Optimeret til at arbejde med Neon PostgreSQL.
 * Bruger PropertyChangeSupport til notifikationer i stedet for EventBus.
 */
public class DatabaseConnection {
    // PropertyChangeSupport til notifikationer
    private static final PropertyChangeSupport support = new PropertyChangeSupport(new DatabaseConnection());

    // Connection pool
    private static ComboPooledDataSource cpds;
    private static boolean initialized = false;
    private static boolean poolClosed = false;

    // Sti til konfigurationsfilen
    private static final String CONFIG_FILE = "src/main/resources/config/database.properties";

    // Forbindelsesstatistik
    private static final AtomicInteger connectionCounter = new AtomicInteger(0);
    private static final AtomicInteger failedConnectionCounter = new AtomicInteger(0);

    // Maksimalt antal genforsøg
    private static final int MAX_RETRY_ATTEMPTS = 3;

    // Beskedtyper for notifikationer
    public static final String DATABASE_ERROR = "DATABASE_ERROR";
    public static final String CONNECTION_ESTABLISHED = "CONNECTION_ESTABLISHED";
    public static final String CONNECTION_FAILED = "CONNECTION_FAILED";
    public static final String POOL_INITIALIZED = "POOL_INITIALIZED";
    public static final String POOL_CLOSED = "POOL_CLOSED";

    static {
        try {
            // Initialiser connection pool
            initializeConnectionPool();
        } catch (Exception e) {
            // Send besked om databasefejl
            Message errorMsg = new Message(DATABASE_ERROR,
                    "Fejl ved initialisering af database forbindelsespool: " + e.getMessage());
            support.firePropertyChange(DATABASE_ERROR, null, errorMsg);
        }
    }

    /**
     * Initialiserer forbindelsespoolen med værdier fra properties-fil eller standardværdier.
     */
    private static void initializeConnectionPool() {
        if (initialized && !poolClosed) {
            return;
        }

        try {
            // Opret en ny connection pool
            cpds = new ComboPooledDataSource();

            // Indlæs konfiguration fra properties-fil
            Properties dbProps = loadDatabaseProperties();

            // Sæt database driver
            cpds.setDriverClass(dbProps.getProperty("db.driver"));

            // Sæt database connection info
            cpds.setJdbcUrl(dbProps.getProperty("db.url"));
            cpds.setUser(dbProps.getProperty("db.user"));
            cpds.setPassword(dbProps.getProperty("db.password"));

            // Konfigurer pool-størrelse
            cpds.setInitialPoolSize(3);  // Start med færre forbindelser for serverless
            cpds.setMinPoolSize(1);      // Minimum kan være lavere for serverless
            cpds.setMaxPoolSize(10);     // Begræns max forbindelser for serverless
            cpds.setAcquireIncrement(1); // Forøg med 1 ad gangen

            // Konfigurer statement caching
            cpds.setMaxStatements(50);

            // Konfigurer forbindelsestest - vigtigt for serverless
            cpds.setIdleConnectionTestPeriod(60);  // Test inaktive forbindelser hvert minut
            cpds.setTestConnectionOnCheckout(true); // Test ved hentning af forbindelse
            cpds.setTestConnectionOnCheckin(true);  // Test ved returnering af forbindelse

            // Konfigurer timeouts - kortere for serverless
            cpds.setCheckoutTimeout(20000);  // 20 sekunder timeout for forbindelseshentning
            cpds.setMaxIdleTime(300);        // 5 minutter max inaktivitetstid
            cpds.setMaxConnectionAge(1800);  // 30 minutter max forbindelsesalder

            // Konfigurer automatisk forbindelsestest
            cpds.setPreferredTestQuery("SELECT 1");

            // Konfigurer retry-indstillinger
            cpds.setAcquireRetryAttempts(5);    // Flere forsøg for serverless
            cpds.setAcquireRetryDelay(500);     // Et halvt sekund mellem forsøg

            // Aktivér forbindelses-reset ved lukning
            cpds.setAutoCommitOnClose(true);

            // Tilføj forbindelsesegenskaber specifikt for Neon
            Properties properties = new Properties();
            properties.setProperty("user", dbProps.getProperty("db.user"));
            properties.setProperty("password", dbProps.getProperty("db.password"));
            cpds.setProperties(properties);

            initialized = true;
            poolClosed = false;



            // Nulstil tællere
            connectionCounter.set(0);
            failedConnectionCounter.set(0);

            // Send besked om initialisering
            Message initMsg = new Message(POOL_INITIALIZED, getPoolStats());
            support.firePropertyChange(POOL_INITIALIZED, null, initMsg);

        } catch (Exception e) {
            // Send besked om databasefejl
            Message errorMsg = new Message(DATABASE_ERROR,
                    "Fejl ved konfiguration af database forbindelsespool: " + e.getMessage());
            support.firePropertyChange(DATABASE_ERROR, null, errorMsg);

            throw new RuntimeException("Kunne ikke initialisere database forbindelsespool", e);
        }
    }

    /**
     * Indlæser database-egenskaber fra konfigurationsfilen.
     *
     * @return Properties-objekt med databasekonfiguration
     */
    private static Properties loadDatabaseProperties() {
        Properties properties = new Properties();
        try {
            // Først forsøg med ClassLoader for at finde filen i classpath
            InputStream inputStream = DatabaseConnection.class.getClassLoader().getResourceAsStream(CONFIG_FILE);

            // Hvis ikke fundet i classpath, prøv med FileInputStream
            if (inputStream == null) {
                inputStream = new FileInputStream(CONFIG_FILE);
            }

            properties.load(inputStream);
            inputStream.close();
        } catch (IOException e) {
            // Læg nogle standard-værdier ind
            properties.setProperty("db.driver", "org.postgresql.Driver");
            properties.setProperty("db.url", "jdbc:postgresql://ep-mute-boat-a9rul5u1-pooler.gwc.azure.neon.tech/neondb?sslmode=require");
            properties.setProperty("db.user", "neondb_owner");
            properties.setProperty("db.password", "npg_6oHRbjLDgK8t");
        }
        return properties;
    }

    /**
     * Privat konstruktør for singleton pattern
     */
    private DatabaseConnection() {
        // Private konstruktør - kan ikke instantieres
    }

    /**
     * Tilføj en lytter til database-hændelser.
     *
     * @param listener PropertyChangeListener der skal tilføjes
     */
    public static void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Fjern en lytter til database-hændelser.
     *
     * @param listener PropertyChangeListener der skal fjernes
     */
    public static void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    /**
     * Tilføj en lytter til specifikke database-hændelser.
     *
     * @param propertyName Navnet på egenskaben at lytte efter
     * @param listener PropertyChangeListener der skal tilføjes
     */
    public static void addListener(String propertyName, PropertyChangeListener listener) {
        support.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Fjern en lytter til specifikke database-hændelser.
     *
     * @param propertyName Navnet på egenskaben at fjerne lytter fra
     * @param listener PropertyChangeListener der skal fjernes
     */
    public static void removeListener(String propertyName, PropertyChangeListener listener) {
        support.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Henter en forbindelse fra poolen med retry-mekanisme.
     *
     * @return Database forbindelse
     * @throws SQLException hvis der er problemer med at etablere forbindelsen
     */
    public static Connection getConnection() throws SQLException {
        if (poolClosed) {
            throw new SQLException("Connection pool er lukket");
        }

        if (!initialized) {
            initializeConnectionPool();
        }

        int attempts = 0;
        SQLException lastException = null;

        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                Connection conn = cpds.getConnection();


                // Send besked om forbindelse oprettet
                Message connMsg = new Message(CONNECTION_ESTABLISHED, connectionCounter.get());
                support.firePropertyChange(CONNECTION_ESTABLISHED, null, connMsg);

                return conn;
            } catch (SQLException e) {
                attempts++;
                lastException = e;



                if (attempts < MAX_RETRY_ATTEMPTS) {
                    try {
                        // Vent før næste forsøg - længere for serverless pga. potentielle cold starts
                        Thread.sleep(1000 * attempts); // Stigende forsinkelse mellem forsøg
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        // Hvis vi når hertil, er alle forsøg mislykkedes
        String errorMsg = "Kunne ikke etablere databaseforbindelse efter " +
                MAX_RETRY_ATTEMPTS + " forsøg";


        // Send besked om forbindelsesfejl
        Message failMsg = new Message(CONNECTION_FAILED,
                errorMsg + ": " + (lastException != null ? lastException.getMessage() : "Ukendt fejl"));
        support.firePropertyChange(CONNECTION_FAILED, null, failMsg);

        throw lastException != null ? lastException :
                new SQLException(errorMsg);
    }

    /**
     * Lukker forbindelsespoolen - kald denne ved programafslutning.
     */
    public static void closePool() {
        if (cpds != null && !poolClosed) {

            // Luk poolen
            cpds.close();

            poolClosed = true;
            initialized = false;


            // Send besked om lukning
            Message closeMsg = new Message(POOL_CLOSED, getPoolStats());
            support.firePropertyChange(POOL_CLOSED, null, closeMsg);
        }
    }

    /**
     * Tester om databasen er tilgængelig.
     *
     * @return true hvis databasen er tilgængelig
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Henter statistik om connection pool status.
     *
     * @return String med pool-statistik
     */
    public static String getPoolStats() {
        try {
            StringBuilder stats = new StringBuilder();
            stats.append("Database Connection Pool Status:\n");
            stats.append("  Forbindelser i brug: ").append(cpds.getNumBusyConnections()).append("\n");
            stats.append("  Inaktive forbindelser: ").append(cpds.getNumIdleConnections()).append("\n");
            stats.append("  Total forbindelser: ").append(cpds.getNumConnections()).append("\n");
            stats.append("  Ventende tråde: ").append(cpds.getThreadPoolNumActiveThreads()).append("\n");
            stats.append("  Total oprettet: ").append(connectionCounter.get()).append("\n");
            stats.append("  Total mislykkede forsøg: ").append(failedConnectionCounter.get());
            return stats.toString();
        } catch (SQLException e) {
            return "Kunne ikke hente pool statistik: " + e.getMessage();
        }
    }
}