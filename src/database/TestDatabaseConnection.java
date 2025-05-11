package database;

import util.Message;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Test-klasse til at verificere databaseforbindelsen.
 */
public class TestDatabaseConnection implements PropertyChangeListener {

    public TestDatabaseConnection() {
        // Registrer som lytter til databasehændelser
        DatabaseConnection.addListener(this);
    }

    /**
     * Tester databaseforbindelsen og udskriver resultat til konsollen.
     */
    public void testConnection() {
        System.out.println("Tester databaseforbindelse...");
        boolean connected = DatabaseConnection.testConnection();
        System.out.println("Forbindelse oprettet: " + connected);

        if (connected) {
            System.out.println(DatabaseConnection.getPoolStats());
        }
    }

    /**
     * Lukker forbindelsespoolen.
     */
    public void closePool() {
        DatabaseConnection.closePool();
    }

    /**
     * Kører en fuld test af databaseforbindelsen og lukker derefter poolen.
     */
    public void runFullTest() {
        testConnection();
        closePool();
    }

    /**
     * Håndterer property change events fra DatabaseConnection.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        Object newValue = evt.getNewValue();

        if (newValue instanceof Message) {
            Message msg = (Message) newValue;
            System.out.println("Modtog databasehændelse: " + msg.getType());
            System.out.println("Indhold: " + msg.getArgs());
        }
    }

    /**
     * Main-metode til at køre en testforbindelse.
     */
    public static void main(String[] args) {
        TestDatabaseConnection tester = new TestDatabaseConnection();
        tester.runFullTest();
    }
}
