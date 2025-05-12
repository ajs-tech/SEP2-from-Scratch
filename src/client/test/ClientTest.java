package client.test;

import client.network.SocketClient;
import client.network.SocketClientImp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import server.model.ServerModel;
import server.model.ServerModelImpl;
import server.network.SocketServer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Test for SocketClient-klassen.
 * Tester klientens evne til at forbinde til serveren, sende anmodninger og modtage svar.
 * Bemærk: Disse tests kræver at serveren kører.
 */
public class ClientTest {
    private SocketServer server;
    private ServerModel serverModel;
    private SocketClient client;
    private final int TEST_PORT = 8880; // Brug en anden port end standard og ServerTest
    private final String TEST_HOST = "localhost";

    @Before
    public void setUp() {
        try {
            // Opret og start en testserver
            serverModel = new ServerModelImpl();
            server = new SocketServer(serverModel, TEST_PORT);
            server.startServer();

            // Vent kort tid for at sikre at serveren er startet
            Thread.sleep(500);

            // Opret en testklient
            client = new SocketClientImp(TEST_HOST, TEST_PORT);
        } catch (Exception e) {
            fail("Kunne ikke opsætte test: " + e.getMessage());
        }
    }

    @After
    public void tearDown() {
        // Luk klienten hvis den findes
        if (client instanceof SocketClientImp) {
            ((SocketClientImp) client).disconnect();
        }

        // Luk serveren hvis den kører
        if (server != null && server.isRunning()) {
            server.stopServer();
        }
    }

    @Test
    public void testClientConnection() {
        // Test at klienten er forbundet til serveren
        if (client instanceof SocketClientImp) {
            assertTrue("Klienten bør være forbundet til serveren",
                    ((SocketClientImp) client).isConnected());
        }
    }

    @Test
    public void testGetAllStudents() {
        // Test at klienten kan hente studerende fra serveren
        // Bemærk: Dette returnerer en tom liste hvis der ikke er studerende i databasen
        assertNotNull("getAllStudents() bør returnere en liste (kan være tom)",
                client.getAllStudents());
    }

    @Test
    public void testGetAvailableLaptops() {
        // Test at klienten kan hente tilgængelige laptops fra serveren
        // Bemærk: Dette returnerer en tom liste hvis der ikke er laptops i databasen
        assertNotNull("getAvailableLaptops() bør returnere en liste (kan være tom)",
                client.getAvailableLaptops());
    }

    @Test
    public void testClientReceivesEvents() throws InterruptedException {
        // Opret en countdown latch for at vente på event
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] eventReceived = {false};

        // Tilføj en listener til klienten
        PropertyChangeListener listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("welcome".equals(evt.getPropertyName())) {
                    eventReceived[0] = true;
                    latch.countDown();
                }
            }
        };

        client.addListener(listener);

        // Giver serveren mulighed for at sende velkommen-besked
        // Dette kan kræve at man sender en ny besked for at udløse velkommen-beskeden igen
        if (client instanceof SocketClientImp) {
            ((SocketClientImp) client).disconnect();
            client = new SocketClientImp(TEST_HOST, TEST_PORT);
            ((SocketClientImp) client).addListener(listener);
        }

        // Vent på at modtage welcome eventet med timeout
        boolean received = latch.await(5, TimeUnit.SECONDS);

        // Fjern listener
        client.removeListener(listener);

        assertTrue("Klienten burde modtage events fra serveren", received && eventReceived[0]);
    }
}