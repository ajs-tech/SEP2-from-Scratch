package test;

import client.network.SocketClient;
import client.network.SocketClientImp;
import enums.PerformanceTypeEnum;
import objects.Laptop;
import objects.Reservation;
import objects.Student;
import org.junit.*;
import server.model.ServerModel;
import server.model.ServerModelImpl;
import server.network.SocketServer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Integrationstest for hele systemet.
 * Tester kommunikation mellem klient og server samt forretningslogik.
 *
 * BEMÆRK: Disse tests kræver en fungerende database-forbindelse.
 */
public class IntegrationTest {
    private static SocketServer server;
    private static ServerModel serverModel;
    private static final int TEST_PORT = 9997; // Brug en anden port end other tests
    private SocketClient client;
    private Student testStudent;
    private Laptop testLaptop;

    @BeforeClass
    public static void setUpClass() {
        try {
            // Opret og start server kun én gang for alle tests
            serverModel = new ServerModelImpl();
            server = new SocketServer(serverModel, TEST_PORT);
            server.startServer();

            // Vent kort tid for at sikre at serveren er startet
            Thread.sleep(1000);
        } catch (Exception e) {
            fail("Kunne ikke starte server: " + e.getMessage());
        }
    }

    @AfterClass
    public static void tearDownClass() {
        // Luk serveren efter alle tests er kørt
        if (server != null && server.isRunning()) {
            server.stopServer();
        }
    }

    @Before
    public void setUp() {
        try {
            // Opret en klient for hver test
            client = new SocketClientImp("localhost", TEST_PORT);

            // Vent kort tid for at sikre at klienten er forbundet
            Thread.sleep(500);

            // Opret testdata
            createTestData();
        } catch (Exception e) {
            fail("Kunne ikke opsætte test: " + e.getMessage());
        }
    }

    @After
    public void tearDown() {
        // Luk klienten
        if (client instanceof SocketClientImp) {
            ((SocketClientImp) client).disconnect();
        }

        // Fjern testdata
        cleanup();
    }

    /**
     * Opretter testdata der bruges i tests
     */
    private void createTestData() {
        try {
            // Opret teststudent med unikke data
            long timestamp = System.currentTimeMillis();
            testStudent = client.createStudent(
                    "Integration Test Student",
                    new Date(System.currentTimeMillis() + 31536000000L), // 1 år fra nu
                    "Integration Test",
                    888888, // Testbruger-id
                    "integrationtest" + timestamp + "@test.com",
                    87654321,
                    PerformanceTypeEnum.LOW
            );

            // Opret testlaptop
            testLaptop = client.createLaptop(
                    "Integration Brand",
                    "Integration Model",
                    250, // GB
                    8,   // RAM
                    PerformanceTypeEnum.LOW
            );

            // Vent kort tid for at sikre at data er oprettet på serveren
            Thread.sleep(500);

        } catch (Exception e) {
            System.err.println("Fejl ved oprettelse af testdata: " + e.getMessage());
        }
    }

    /**
     * Rydder op efter tests
     */
    private void cleanup() {
        try {
            // Fjern teststudent
            if (testStudent != null) {
                client.deleteStudent(testStudent.getViaId());
            }

            // Fjern testlaptop
            if (testLaptop != null) {
                client.deleteLaptop(testLaptop.getId());
            }
        } catch (Exception e) {
            System.err.println("Fejl ved oprydning: " + e.getMessage());
        }
    }

    @Test
    public void testEndToEndCreateAndGetStudent() {
        // Test at teststudenten blev oprettet
        assertNotNull("Teststudent bør være oprettet", testStudent);

        // Hent alle studerende fra serveren via klienten
        List<Student> allStudents = client.getAllStudents();

        // Find teststudenten i listen
        boolean found = false;
        for (Student student : allStudents) {
            if (student.getViaId() == testStudent.getViaId()) {
                found = true;
                break;
            }
        }

        assertTrue("Teststudent bør være i listen af alle studerende", found);
    }

    @Test
    public void testEndToEndCreateReservation() throws InterruptedException {
        // Dette test forudsætter at både student og laptop er oprettet
        if (testStudent == null || testLaptop == null) {
            fail("Kan ikke teste reservation uden gyldig student og laptop");
            return;
        }

        // Forbered til at lytte efter reservations-events
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] reservationReceived = {false};

        PropertyChangeListener listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("reservation_created".equals(evt.getPropertyName())) {
                    reservationReceived[0] = true;
                    latch.countDown();
                }
            }
        };

        client.addListener(listener);

        // Opret en reservation
        Reservation reservation = client.createReservation(testStudent, testLaptop);

        // Vent på at modtage reservations-event
        boolean received = latch.await(5, TimeUnit.SECONDS);

        // Fjern listener
        client.removeListener(listener);

        // Verificer at reservation blev oprettet
        assertNotNull("Reservation bør være oprettet", reservation);
        assertTrue("Klienten burde modtage reservation_created event",
                received && reservationReceived[0]);

        // Tjek at laptop nu er udlånt
        List<Laptop> loanedLaptops = client.getLoanedLaptops();

        boolean laptopLoaned = false;
        for (Laptop laptop : loanedLaptops) {
            if (laptop.getId().equals(testLaptop.getId())) {
                laptopLoaned = true;
                break;
            }
        }

        assertTrue("Testlaptop bør være i listen af udlånte laptops", laptopLoaned);
    }

    @Test
    public void testEndToEndQueueFunctionality() throws InterruptedException {
        // Først opretter vi en student med høj performance behov
        long timestamp = System.currentTimeMillis();
        Student highPerfStudent = client.createStudent(
                "Queue Test Student",
                new Date(System.currentTimeMillis() + 31536000000L), // 1 år fra nu
                "Queue Test",
                777777, // Testbruger-id
                "queuetest" + timestamp + "@test.com",
                11112222,
                PerformanceTypeEnum.HIGH
        );

        assertNotNull("High perf student bør være oprettet", highPerfStudent);

        // Forbered til at lytte efter queue-events
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] queueEventReceived = {false};

        PropertyChangeListener listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().contains("queue")) {
                    queueEventReceived[0] = true;
                    latch.countDown();
                }
            }
        };

        client.addListener(listener);

        // Tilføj student til høj performance kø
        boolean added = client.addToHighPerformanceQueue(highPerfStudent.getViaId());

        // Vent på at modtage queue-event
        boolean received = latch.await(5, TimeUnit.SECONDS);

        // Fjern listener
        client.removeListener(listener);

        // Oprydning - fjern teststudent
        client.deleteStudent(highPerfStudent.getViaId());

        // Verificer resultater
        assertTrue("Student burde kunne tilføjes til kø", added);
        assertTrue("Klienten burde modtage queue-event",
                received && queueEventReceived[0]);
    }

    @Test
    public void testServerBroadcastToMultipleClients() throws InterruptedException {
        // Opret en ekstra klient
        SocketClient secondClient = new SocketClientImp("localhost", TEST_PORT);

        try {
            // Forbered til at lytte efter events på begge klienter
            final CountDownLatch latch = new CountDownLatch(2); // Venter på events fra begge klienter
            final boolean[] firstClientReceived = {false};
            final boolean[] secondClientReceived = {false};

            PropertyChangeListener firstListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("laptop_created")) {
                        firstClientReceived[0] = true;
                        latch.countDown();
                    }
                }
            };

            PropertyChangeListener secondListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("laptop_created")) {
                        secondClientReceived[0] = true;
                        latch.countDown();
                    }
                }
            };

            client.addListener(firstListener);
            secondClient.addListener(secondListener);

            // Opret en ny laptop fra den første klient (dette bør udløse events på begge klienter)
            long timestamp = System.currentTimeMillis();
            Laptop broadcastTestLaptop = client.createLaptop(
                    "Broadcast Test",
                    "Model " + timestamp,
                    500, // GB
                    16,  // RAM
                    PerformanceTypeEnum.HIGH
            );

            // Vent på at begge klienter modtager eventet
            boolean bothReceived = latch.await(5, TimeUnit.SECONDS);

            // Fjern listeners
            client.removeListener(firstListener);
            secondClient.removeListener(secondListener);

            // Oprydning
            if (broadcastTestLaptop != null) {
                client.deleteLaptop(broadcastTestLaptop.getId());
            }

            // Verificer resultater
            assertTrue("Begge klienter burde modtage events", bothReceived);
            assertTrue("Første klient burde modtage events", firstClientReceived[0]);
            assertTrue("Anden klient burde modtage events", secondClientReceived[0]);

        } finally {
            // Luk den ekstra klient
            if (secondClient instanceof SocketClientImp) {
                ((SocketClientImp) secondClient).disconnect();
            }
        }
    }
}