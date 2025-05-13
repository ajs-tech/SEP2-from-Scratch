package test;

import client.network.SocketClient;
import client.network.SocketClientImp;
import database.DatabaseConnection;
import database.LaptopDAO;
import database.QueueDAO;
import database.ReservationDAO;
import database.StudentDAO;
import enums.PerformanceTypeEnum;
import enums.ReservationStatusEnum;
import objects.Laptop;
import objects.Reservation;
import objects.Student;
import org.junit.*;
import server.model.ServerModel;
import server.model.ServerModelImpl;
import server.network.SocketServer;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Detaljeret test af student-laptop assignment med omfattende logging
 * for fejlfinding og dybdegående verificering af alle scenarier.
 */
public class DetailedStudentLaptopAssignmentTest {
    private static SocketServer server;
    private static ServerModel serverModel;
    private static final int TEST_PORT = 8890;
    private SocketClient client;

    // Direct DAO access for debugging
    private StudentDAO studentDAO;
    private LaptopDAO laptopDAO;
    private ReservationDAO reservationDAO;
    private QueueDAO queueDAO;

    private static int testRunId = 0;

    @BeforeClass
    public static void setUpClass() {
        try {
            // Generate unique test run ID
            testRunId = (int) (System.currentTimeMillis() % 100000);
            System.out.println("=== Test Run ID: " + testRunId + " ===");

            // Start server for tests
            serverModel = new ServerModelImpl();
            server = new SocketServer(serverModel, TEST_PORT);
            server.startServer();
            Thread.sleep(1000);

            System.out.println("Server started successfully on port " + TEST_PORT);
        } catch (Exception e) {
            fail("Could not start server: " + e.getMessage());
        }
    }

    @AfterClass
    public static void tearDownClass() {
        if (server != null && server.isRunning()) {
            server.stopServer();
            System.out.println("Server stopped");
        }
    }

    @Before
    public void setUp() {
        try {
            System.out.println("\n--- Setting up test ---");

            // Initialize DAOs for direct database access
            studentDAO = new StudentDAO();
            laptopDAO = new LaptopDAO();
            reservationDAO = new ReservationDAO();
            queueDAO = new QueueDAO();

            // Clean database before test
            cleanDatabase();

            // Create client connection
            client = new SocketClientImp("localhost", TEST_PORT);
            Thread.sleep(500);

            System.out.println("Client connected and database cleaned");
        } catch (Exception e) {
            fail("Could not set up test: " + e.getMessage());
        }
    }

    @After
    public void tearDown() {
        System.out.println("\n--- Tearing down test ---");

        if (client instanceof SocketClientImp) {
            ((SocketClientImp) client).disconnect();
        }

        // Clean database after test
        cleanDatabase();

        System.out.println("Test cleanup completed");
    }

    /**
     * Test 1: Detailed test of automatic laptop assignment when available
     */
    @Test
    public void testAutomaticLaptopAssignmentWhenAvailable() throws Exception {
        System.out.println("\n=== TEST 1: Automatic Laptop Assignment ===");

        // Step 1: Create laptops
        System.out.println("Step 1: Creating laptops...");

        Laptop highLaptop = createAndVerifyLaptop("Dell", "Precision 7760", 1024, 32, PerformanceTypeEnum.HIGH);
        Laptop lowLaptop = createAndVerifyLaptop("HP", "ProBook 450", 256, 8, PerformanceTypeEnum.LOW);

        System.out.println("Created high-performance laptop: " + highLaptop.getId());
        System.out.println("Created low-performance laptop: " + lowLaptop.getId());

        // Verify laptops are available
        verifyLaptopAvailability(highLaptop.getId(), true);
        verifyLaptopAvailability(lowLaptop.getId(), true);

        // Step 2: Create student requiring high-performance laptop
        System.out.println("\nStep 2: Creating high-performance student...");

        int viaId = 200000 + testRunId;
        Student student = client.createStudent(
                "Alice Test",
                new Date(System.currentTimeMillis() + 31536000000L),
                "Computer Science",
                viaId,
                "alice" + testRunId + "@test.com",
                12345678,
                PerformanceTypeEnum.HIGH
        );

        assertNotNull("Student should be created", student);
        System.out.println("Created student: " + student.getName() + " (VIA ID: " + student.getViaId() + ")");

        // Step 3: Wait for automatic assignment
        System.out.println("\nStep 3: Waiting for automatic assignment...");
        Thread.sleep(2000); // Give server time to process

        // Step 4: Verify reservation was created
        System.out.println("\nStep 4: Verifying reservation...");

        List<Reservation> activeReservations = reservationDAO.getAllActive();
        System.out.println("Active reservations count: " + activeReservations.size());

        boolean foundReservation = false;
        Reservation studentReservation = null;

        for (Reservation res : activeReservations) {
            System.out.println("Checking reservation: Student " + res.getStudent().getViaId() +
                    " has laptop " + res.getLaptop().getId());
            if (res.getStudent().getViaId() == student.getViaId()) {
                foundReservation = true;
                studentReservation = res;
                break;
            }
        }

        assertTrue("Student should have a reservation", foundReservation);
        assertNotNull("Student reservation should not be null", studentReservation);
        assertEquals("Student should get high-performance laptop",
                highLaptop.getId(), studentReservation.getLaptop().getId());

        // Step 5: Verify laptop is no longer available
        System.out.println("\nStep 5: Verifying laptop availability...");

        verifyLaptopAvailability(highLaptop.getId(), false);
        verifyLaptopAvailability(lowLaptop.getId(), true); // Low laptop should still be available

        System.out.println("✓ Test 1 completed successfully");
    }

    /**
     * Test 2: Detailed test of queue placement when no laptops available
     */
    @Test
    public void testQueuePlacementWhenNoLaptopsAvailable() throws Exception {
        System.out.println("\n=== TEST 2: Queue Placement When No Laptops ===");

        // Step 1: Create no laptops
        System.out.println("Step 1: No laptops created (simulating all laptops loaned out)");

        // Verify no available laptops
        List<Laptop> availableLaptops = laptopDAO.getAvailableLaptops();
        System.out.println("Available laptops count: " + availableLaptops.size());
        assertEquals("Should have no available laptops", 0, availableLaptops.size());

        // Step 2: Create high-performance student
        System.out.println("\nStep 2: Creating high-performance student...");

        int highViaId = 210000 + testRunId;
        Student highStudent = client.createStudent(
                "Bob HighPerf",
                new Date(System.currentTimeMillis() + 31536000000L),
                "Software Engineering",
                highViaId,
                "bob.high" + testRunId + "@test.com",
                87654321,
                PerformanceTypeEnum.HIGH
        );

        assertNotNull("High-performance student should be created", highStudent);
        System.out.println("Created high-performance student: " + highStudent.getName());

        // Step 3: Verify student is in high-performance queue
        System.out.println("\nStep 3: Verifying high-performance queue...");
        Thread.sleep(1000);

        List<Student> highQueue = queueDAO.getQueueByPerformanceType(PerformanceTypeEnum.HIGH);
        System.out.println("High-performance queue size: " + highQueue.size());

        boolean foundInHighQueue = false;
        for (Student s : highQueue) {
            System.out.println("Student in high queue: " + s.getName() + " (VIA ID: " + s.getViaId() + ")");
            if (s.getViaId() == highStudent.getViaId()) {
                foundInHighQueue = true;
            }
        }

        assertTrue("Student should be in high-performance queue", foundInHighQueue);

        // Step 4: Create low-performance student
        System.out.println("\nStep 4: Creating low-performance student...");

        int lowViaId = 220000 + testRunId;
        Student lowStudent = client.createStudent(
                "Carol LowPerf",
                new Date(System.currentTimeMillis() + 31536000000L),
                "Business IT",
                lowViaId,
                "carol.low" + testRunId + "@test.com",
                11223344,
                PerformanceTypeEnum.LOW
        );

        assertNotNull("Low-performance student should be created", lowStudent);
        System.out.println("Created low-performance student: " + lowStudent.getName());

        // Step 5: Verify student is in low-performance queue
        System.out.println("\nStep 5: Verifying low-performance queue...");
        Thread.sleep(1000);

        List<Student> lowQueue = queueDAO.getQueueByPerformanceType(PerformanceTypeEnum.LOW);
        System.out.println("Low-performance queue size: " + lowQueue.size());

        boolean foundInLowQueue = false;
        for (Student s : lowQueue) {
            System.out.println("Student in low queue: " + s.getName() + " (VIA ID: " + s.getViaId() + ")");
            if (s.getViaId() == lowStudent.getViaId()) {
                foundInLowQueue = true;
            }
        }

        assertTrue("Student should be in low-performance queue", foundInLowQueue);

        // Step 6: Verify no reservations were created
        System.out.println("\nStep 6: Verifying no reservations created...");

        List<Reservation> reservations = reservationDAO.getAllActive();
        System.out.println("Active reservations count: " + reservations.size());
        assertEquals("Should have no active reservations", 0, reservations.size());

        System.out.println("✓ Test 2 completed successfully");
    }

    /**
     * Test 3: Queue processing when laptop becomes available
     */
    @Test
    public void testQueueProcessingWhenLaptopAvailable() throws Exception {
        System.out.println("\n=== TEST 3: Queue Processing When Laptop Available ===");

        // Step 1: Create student and add to queue (no laptops available)
        System.out.println("Step 1: Creating student with no laptops available...");

        int viaId = 230000 + testRunId;
        Student queuedStudent = client.createStudent(
                "David Queued",
                new Date(System.currentTimeMillis() + 31536000000L),
                "Data Science",
                viaId,
                "david.queue" + testRunId + "@test.com",
                55667788,
                PerformanceTypeEnum.HIGH
        );

        assertNotNull("Student should be created", queuedStudent);
        Thread.sleep(1000);

        // Verify student is in queue
        List<Student> queue = queueDAO.getQueueByPerformanceType(PerformanceTypeEnum.HIGH);
        assertTrue("Student should be in queue",
                queue.stream().anyMatch(s -> s.getViaId() == queuedStudent.getViaId()));

        // Step 2: Create a laptop
        System.out.println("\nStep 2: Creating high-performance laptop...");

        Laptop laptop = createAndVerifyLaptop("Dell", "XPS 15", 512, 16, PerformanceTypeEnum.HIGH);
        System.out.println("Created laptop: " + laptop.getId());

        // Step 3: Process queues
        System.out.println("\nStep 3: Processing queues...");

        int processed = client.processQueues();
        System.out.println("Processed assignments: " + processed);

        Thread.sleep(1000);

        // Step 4: Verify student got the laptop
        System.out.println("\nStep 4: Verifying assignment...");

        List<Reservation> reservations = reservationDAO.getAllActive();
        boolean foundReservation = false;

        for (Reservation res : reservations) {
            if (res.getStudent().getViaId() == queuedStudent.getViaId()) {
                foundReservation = true;
                assertEquals("Student should get the created laptop",
                        laptop.getId(), res.getLaptop().getId());
            }
        }

        assertTrue("Student should have a reservation after queue processing", foundReservation);

        // Verify queue is now empty
        queue = queueDAO.getQueueByPerformanceType(PerformanceTypeEnum.HIGH);
        assertFalse("Student should no longer be in queue",
                queue.stream().anyMatch(s -> s.getViaId() == queuedStudent.getViaId()));

        System.out.println("✓ Test 3 completed successfully");
    }

    // Helper methods

    private Laptop createAndVerifyLaptop(String brand, String model, int disk, int ram,
                                         PerformanceTypeEnum type) throws Exception {
        Laptop laptop = client.createLaptop(brand, model, disk, ram, type);
        assertNotNull("Laptop should be created", laptop);

        // Verify in database
        Laptop dbLaptop = laptopDAO.getById(laptop.getId());
        assertNotNull("Laptop should exist in database", dbLaptop);
        assertEquals("Brand should match", brand, dbLaptop.getBrand());
        assertEquals("Model should match", model, dbLaptop.getModel());

        return laptop;
    }

    private void verifyLaptopAvailability(UUID laptopId, boolean shouldBeAvailable) throws Exception {
        Laptop laptop = laptopDAO.getById(laptopId);
        assertNotNull("Laptop should exist", laptop);

        if (shouldBeAvailable) {
            assertTrue("Laptop should be available", laptop.isAvailable());
            assertFalse("Laptop should not be loaned", laptop.isLoaned());
        } else {
            assertFalse("Laptop should not be available", laptop.isAvailable());
            assertTrue("Laptop should be loaned", laptop.isLoaned());
        }

        System.out.println("Laptop " + laptopId + " availability: " +
                (laptop.isAvailable() ? "AVAILABLE" : "LOANED"));
    }

    private void cleanDatabase() {
        try {
            Connection conn = DatabaseConnection.getConnection();

            // Delete all data in correct order
            String[] cleanupQueries = {
                    "DELETE FROM QueueEntry",
                    "DELETE FROM Reservation",
                    "DELETE FROM Student",
                    "DELETE FROM Laptop"
            };

            for (String query : cleanupQueries) {
                try (Statement stmt = conn.createStatement()) {
                    int deleted = stmt.executeUpdate(query);
                    System.out.println("Deleted " + deleted + " rows: " + query);
                } catch (Exception e) {
                    System.err.println("Error cleaning: " + query + " - " + e.getMessage());
                }
            }

            conn.close();
        } catch (Exception e) {
            System.err.println("Error cleaning database: " + e.getMessage());
        }
    }
}