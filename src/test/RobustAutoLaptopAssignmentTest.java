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

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Robust test af automatisk laptop-tildeling og kø-system
 * Håndterer eksisterende data i databasen
 */
public class RobustAutoLaptopAssignmentTest {
    private static SocketServer server;
    private static ServerModel serverModel;
    private static final int TEST_PORT = 8890;
    private SocketClient client;
    private Random random = new Random();

    @BeforeClass
    public static void setUpClass() {
        try {
            serverModel = new ServerModelImpl();
            server = new SocketServer(serverModel, TEST_PORT);
            server.startServer();
            Thread.sleep(1000);
            System.out.println("Test server started on port " + TEST_PORT);
        } catch (Exception e) {
            fail("Could not start server: " + e.getMessage());
        }
    }

    @AfterClass
    public static void tearDownClass() {
        if (server != null && server.isRunning()) {
            server.stopServer();
        }
    }

    @Before
    public void setUp() {
        try {
            client = new SocketClientImp("localhost", TEST_PORT);
            Thread.sleep(500);
        } catch (Exception e) {
            fail("Could not set up test: " + e.getMessage());
        }
    }

    @After
    public void tearDown() {
        if (client instanceof SocketClientImp) {
            ((SocketClientImp) client).disconnect();
        }
    }

    /**
     * Test case 1: Student får en laptop automatisk (ikke nødvendigvis den vi opretter)
     */
    @Test
    public void testAutomaticLaptopAssignment() throws Exception {
        System.out.println("\n=== Test: Automatic Laptop Assignment ===");

        int testId = 600000 + random.nextInt(100000);

        // Tjek hvor mange høj-ydelses laptops der er ledige FØR vi opretter noget
        List<Laptop> availableBefore = client.getAvailableLaptops().stream()
                .filter(l -> l.getPerformanceType() == PerformanceTypeEnum.HIGH)
                .collect(Collectors.toList());

        System.out.println("Available high-performance laptops before: " + availableBefore.size());

        // Opret en student
        Student student = client.createStudent(
                "Test Student " + testId,
                new Date(System.currentTimeMillis() + 31536000000L),
                "Computer Science",
                testId,
                "test" + testId + "@test.com",
                10000000 + testId,
                PerformanceTypeEnum.HIGH
        );

        assertNotNull("Student should be created", student);
        System.out.println("Created student: " + student.getViaId());

        Thread.sleep(1500);

        // Tjek om student fik en laptop
        List<Reservation> reservations = client.getActiveReservations();

        boolean foundReservation = false;
        for (Reservation res : reservations) {
            if (res.getStudent().getViaId() == student.getViaId()) {
                foundReservation = true;
                System.out.println("✓ Student got laptop: " + res.getLaptop().getId());
                assertEquals("Laptop should be high-performance",
                        PerformanceTypeEnum.HIGH,
                        res.getLaptop().getPerformanceType());
                break;
            }
        }

        if (!foundReservation && availableBefore.isEmpty()) {
            // Hvis ingen laptops var ledige, tjek om student er i kø
            List<Student> queue = client.getHighPerformanceQueue();
            boolean inQueue = queue.stream().anyMatch(s -> s.getViaId() == student.getViaId());
            assertTrue("Student should be in queue when no laptops available", inQueue);
            System.out.println("✓ Student correctly placed in queue");
        } else {
            assertTrue("Student should have a reservation when laptops were available", foundReservation);
        }
    }

    /**
     * Test case 2: Test kø-system når vi er sikre på ingen laptops er ledige
     */
    @Test
    public void testQueueSystem() throws Exception {
        System.out.println("\n=== Test: Queue System ===");

        int testId = 700000 + random.nextInt(100000);

        // Loan ud ALLE low-performance laptops først
        List<Laptop> availableLowLaptops = client.getAvailableLaptops().stream()
                .filter(l -> l.getPerformanceType() == PerformanceTypeEnum.LOW)
                .collect(Collectors.toList());

        System.out.println("Available low-performance laptops: " + availableLowLaptops.size());

        // Opret studenter til at låne alle ledige laptops
        for (int i = 0; i < availableLowLaptops.size(); i++) {
            Student loanStudent = client.createStudent(
                    "Loan Student " + (testId + i),
                    new Date(System.currentTimeMillis() + 31536000000L),
                    "Loan Test",
                    testId + i,
                    "loan" + (testId + i) + "@test.com",
                    50000000 + testId + i,
                    PerformanceTypeEnum.LOW
            );
            Thread.sleep(500);
        }

        // Vent på at alle laptops bliver lånt ud
        Thread.sleep(2000);

        // NU bør alle low-performance laptops være udlånt
        List<Laptop> availableNow = client.getAvailableLaptops().stream()
                .filter(l -> l.getPerformanceType() == PerformanceTypeEnum.LOW)
                .collect(Collectors.toList());

        System.out.println("Available low-performance laptops now: " + availableNow.size());

        // Opret en ny student der vil blive sat i kø
        int queueTestId = testId + 1000;
        Student queueStudent = client.createStudent(
                "Queue Student " + queueTestId,
                new Date(System.currentTimeMillis() + 31536000000L),
                "Queue Test",
                queueTestId,
                "queue" + queueTestId + "@test.com",
                60000000 + queueTestId,
                PerformanceTypeEnum.LOW
        );

        assertNotNull("Student should be created", queueStudent);
        Thread.sleep(1500);

        // Tjek om student er i kø
        List<Student> queue = client.getLowPerformanceQueue();
        boolean foundInQueue = queue.stream().anyMatch(s -> s.getViaId() == queueStudent.getViaId());

        if (!foundInQueue && availableNow.size() > 0) {
            // Hvis ikke i kø, må student have fået en laptop
            List<Reservation> reservations = client.getActiveReservations();
            boolean hasReservation = reservations.stream()
                    .anyMatch(r -> r.getStudent().getViaId() == queueStudent.getViaId());
            assertTrue("Student should have reservation if not in queue", hasReservation);
            System.out.println("✓ Student got an available laptop");
        } else {
            assertTrue("Student should be in queue when no laptops available", foundInQueue);
            System.out.println("✓ Student correctly placed in queue");
        }
    }

    /**
     * Test case 3: Test at kø behandles korrekt
     */
    @Test
    public void testQueueProcessing() throws Exception {
        System.out.println("\n=== Test: Queue Processing ===");

        int testId = 800000 + random.nextInt(100000);

        // Opret en student uden at bekymre os om den går i kø eller ej
        Student student = client.createStudent(
                "Process Student " + testId,
                new Date(System.currentTimeMillis() + 31536000000L),
                "Process Test",
                testId,
                "process" + testId + "@test.com",
                70000000 + testId,
                PerformanceTypeEnum.HIGH
        );

        assertNotNull("Student should be created", student);
        Thread.sleep(1500);

        // Tjek studenten's status
        List<Reservation> reservations = client.getActiveReservations();
        boolean hasReservation = reservations.stream()
                .anyMatch(r -> r.getStudent().getViaId() == student.getViaId());

        List<Student> queue = client.getHighPerformanceQueue();
        boolean inQueue = queue.stream().anyMatch(s -> s.getViaId() == student.getViaId());

        System.out.println("Student has reservation: " + hasReservation);
        System.out.println("Student in queue: " + inQueue);

        if (inQueue) {
            // Hvis i kø, opret en laptop og kør processing
            Laptop laptop = client.createLaptop(
                    "Process Test",
                    "Model " + testId,
                    512,
                    16,
                    PerformanceTypeEnum.HIGH
            );

            assertNotNull("Laptop should be created", laptop);
            Thread.sleep(500);

            int processed = client.processQueues();
            System.out.println("Processed: " + processed + " assignments");

            Thread.sleep(1500);

            // Tjek om student nu har en reservation
            reservations = client.getActiveReservations();
            hasReservation = reservations.stream()
                    .anyMatch(r -> r.getStudent().getViaId() == student.getViaId());

            assertTrue("Student should have reservation after processing", hasReservation);
            System.out.println("✓ Queue processing successful");
        } else {
            assertTrue("Student should have reservation if not in queue", hasReservation);
            System.out.println("✓ Student already had a laptop");
        }
    }
}