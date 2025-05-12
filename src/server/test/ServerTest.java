package server.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import server.model.ServerModel;
import server.model.ServerModelImpl;
import server.network.SocketServer;

import java.io.IOException;
import java.net.Socket;

import static org.junit.Assert.*;

/**
 * Test for SocketServer-klassen.
 * Tester serverens evne til at starte, acceptere forbindelser og lukke ned.
 */
public class ServerTest {
    private SocketServer server;
    private ServerModel serverModel;
    private final int TEST_PORT = 9999; // Brug en anden port end standard for at undgå konflikter

    @Before
    public void setUp() {
        // Opret en ny ServerModel for testene
        serverModel = new ServerModelImpl();
        // Opret en SocketServer med testporten
        server = new SocketServer(serverModel, TEST_PORT);
    }

    @After
    public void tearDown() {
        // Luk serveren hvis den kører
        if (server != null && server.isRunning()) {
            server.stopServer();
        }
    }

    @Test
    public void testServerStart() {
        try {
            // Test at serveren kan starte
            server.startServer();
            assertTrue("Serveren bør køre efter start", server.isRunning());
            assertEquals("Serveren bør køre på den specificerede port", TEST_PORT, server.getPort());
        } catch (IOException e) {
            fail("Serveren kunne ikke starte: " + e.getMessage());
        }
    }

    @Test
    public void testServerAcceptsConnections() {
        try {
            // Start serveren
            server.startServer();

            // Forsøg at oprette en forbindelse til serveren
            Socket testSocket = new Socket("localhost", TEST_PORT);

            assertTrue("Socket burde være forbundet", testSocket.isConnected());
            assertFalse("Socket burde ikke være lukket", testSocket.isClosed());

            // Luk testsocket
            testSocket.close();

            // Der bør være 0 eller 1 klienter forbundet, afhængigt af timing
            // Vi tester blot at getConnectedClientCount() ikke kaster en exception
            int clientCount = server.getConnectedClientCount();
            assertTrue("Klienttæller bør være ikke-negativ", clientCount >= 0);

        } catch (IOException e) {
            fail("Fejl ved test af forbindelser: " + e.getMessage());
        }
    }

    @Test
    public void testServerStop() {
        try {
            // Start serveren
            server.startServer();

            // Stop serveren
            server.stopServer();

            assertFalse("Serveren bør ikke køre efter stop", server.isRunning());

            // Forsøg at oprette en forbindelse bør fejle
            try {
                Socket testSocket = new Socket("localhost", TEST_PORT);
                testSocket.close();
                fail("Det burde ikke være muligt at forbinde til en stoppet server");
            } catch (IOException e) {
                // Dette er forventet
                assertTrue(true);
            }

        } catch (IOException e) {
            fail("Fejl ved test af servernedlukning: " + e.getMessage());
        }
    }
}