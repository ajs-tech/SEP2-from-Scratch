package test;

import client.network.SocketClient;
import client.network.SocketClientImp;
import enums.PerformanceTypeEnum;
import enums.ReservationStatusEnum;
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

import static org.junit.Assert.*;

/**
 * Komplet test af student oprettelse med automatisk laptop tildeling eller queue placering.
 * Tester alle scenarier:
 * 1. Student gets laptop immediately when available
 * 2. Student goes to queue when no laptops available
 * 3. Queue processing when laptops become available
 */
public class CompleteStudentLaptopAssignmentTest {
    private static SocketServer server;
    private static ServerModel serverModel;
    private static final int TEST_PORT = 8886;
    private SocketClient client;
    private Random random = new Random();

    @BeforeClass
    public static void setUpClass() {
        try {
            // Start server for tests
            serverModel = new ServerModelImpl();
            server = new SocketServer(serverModel, TEST_PORT);
            server.startServer();
            Thread.sleep(1000);
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
            // Create client connection
            client = new SocketClientImp("localhost", TEST_PORT);
            Thread.sleep(500);

            // Clear all existing data
            clearAllData();
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
     * Test 1: Student gets laptop immediately when available
     */
    @Test
    public void testStudentGetsLaptopWhenAvailable() throws InterruptedException {
        System.out.println("\n=== TEST 1: Student gets laptop when available ===");

        // Create one high-performance laptop
        Laptop highLaptop = client.createLaptop("Dell", "Precision 7760", 1024, 32, PerformanceTypeEnum.HIGH);
        assertNotNull("High-performance laptop should be created", highLaptop);
        assertTrue("Laptop should be available", highLaptop.isAvailable());

        // Create one low-performance laptop
        Laptop lowLaptop = client.createLaptop("HP", "ProBook 450", 256, 8, PerformanceTypeEnum.LOW);
        assertNotNull("Low-performance laptop should be created", lowLaptop);
        assertTrue("Laptop should be available", lowLaptop.isAvailable());

        Thread.sleep(500); // Wait for database sync

        // Create student needing high-performance laptop
        Student highPerfStudent = createTestStudent("Alice", "ali@test.com", 101101, PerformanceTypeEnum.HIGH);
        assertNotNull("High-performance student should be created", highPerfStudent);

        Thread.sleep(1000); // Wait for automatic assignment

        // Verify student got the laptop
        List<Reservation> activeReservations = client.getActiveReservations();
        assertEquals("Should have one active reservation", 1, activeReservations.size());

        Reservation reservation = activeReservations.get(0);
        assertEquals("Student should match", highPerfStudent.getViaId(), reservation.getStudent().getViaId());
        assertEquals("Laptop should match", highLaptop.getId(), reservation.getLaptop().getId());
        assertEquals("Reservation should be active", ReservationStatusEnum.ACTIVE, reservation.getStatus());

        // Verify laptop is no longer available
        List<Laptop> availableLaptops = client.getAvailableLaptops();
        assertEquals("Should have one available laptop left", 1, availableLaptops.size());
        assertEquals("Low-performance laptop should still be available", lowLaptop.getId(), availableLaptops.get(0).getId());

        System.out.println("✓ Student got laptop immediately");
    }

    /**
     * Test 2: Student goes to queue when no laptops available
     */
    @Test
    public void testStudentGoesToQueueWhenNoLaptops() throws InterruptedException {
        System.out.println("\n=== TEST 2: Student goes to queue when no laptops ===");

        // Create no laptops initially

        // Create high-performance student
        Student student1 = createTestStudent("Bob", "bobeeeeee@test.com", 233332, PerformanceTypeEnum.HIGH);
        assertNotNull("Student should be created", student1);

        Thread.sleep(1000); // Wait for queue placement

        // Verify student is in queue
        List<Student> highQueue = client.getHighPerformanceQueue();
        assertEquals("Should have one student in high-performance queue", 1, highQueue.size());
        assertEquals("Student should be in queue", student1.getViaId(), highQueue.get(0).getViaId());

        // Create low-performance student
        Student student2 = createTestStudent("Carol", "carol@test.com", 100003, PerformanceTypeEnum.LOW);
        assertNotNull("Low-performance student should be created", student2);

        Thread.sleep(1000); // Wait for queue placement

        // Verify student is in low queue
        List<Student> lowQueue = client.getLowPerformanceQueue();
        assertEquals("Should have one student in low-performance queue", 1, lowQueue.size());
        assertEquals("Student should be in queue", student2.getViaId(), lowQueue.get(0).getViaId());

        System.out.println("✓ Students placed in correct queues");
    }

    /**
     * Test 3: Multiple students, some get laptops, others go to queue
     */
    @Test
    public void testMixedScenario() throws InterruptedException {
        System.out.println("\n=== TEST 3: Mixed scenario - some get laptops, others queue ===");

        // Create 2 high-performance laptops
        for (int i = 0; i < 2; i++) {
            Laptop laptop = client.createLaptop("Dell", "Precision " + i, 1024, 32, PerformanceTypeEnum.HIGH);
            assertNotNull("Laptop should be created", laptop);
        }

        // Create 1 low-performance laptop
        Laptop lowLaptop = client.createLaptop("HP", "ProBook", 256, 8, PerformanceTypeEnum.LOW);
        assertNotNull("Low laptop should be created", lowLaptop);

        Thread.sleep(500);

        // Create 4 high-performance students (only 2 laptops available)
        for (int i = 1; i <= 4; i++) {
            Student student = createTestStudent("HighStudent" + i, "high" + i + "@test.com",
                    100010 + i, PerformanceTypeEnum.HIGH);
            assertNotNull("Student should be created", student);
            Thread.sleep(500); // Small delay between creations
        }

        // Create 2 low-performance students (only 1 laptop available)
        for (int i = 1; i <= 2; i++) {
            Student student = createTestStudent("LowStudent" + i, "low" + i + "@test.com",
                    100020 + i, PerformanceTypeEnum.LOW);
            assertNotNull("Student should be created", student);
            Thread.sleep(500);
        }

        Thread.sleep(2000); // Wait for all assignments and queue placements

        // Verify active reservations
        List<Reservation> activeReservations = client.getActiveReservations();
        assertEquals("Should have 3 active reservations (2 high + 1 low)", 3, activeReservations.size());

        // Verify queues
        List<Student> highQueue = client.getHighPerformanceQueue();
        assertEquals("Should have 2 students in high-performance queue", 2, highQueue.size());

        List<Student> lowQueue = client.getLowPerformanceQueue();
        assertEquals("Should have 1 student in low-performance queue", 1, lowQueue.size());

        System.out.println("✓ Mixed scenario handled correctly");
        System.out.println("  - Active reservations: " + activeReservations.size());
        System.out.println("  - High queue: " + highQueue.size());
        System.out.println("  - Low queue: " + lowQueue.size());
    }

    /**
     * Test 4: Queue processing when laptop becomes available
     */
    @Test
    public void testQueueProcessingWhenLaptopReturned() throws InterruptedException {
        System.out.println("\n=== TEST 4: Queue processing when laptop returned ===");

        // Create one laptop and assign it
        Laptop laptop = client.createLaptop("Dell", "Precision", 1024, 32, PerformanceTypeEnum.HIGH);
        assertNotNull("Laptop should be created", laptop);

        Student student1 = createTestStudent("First", "first@test.com", 100030, PerformanceTypeEnum.HIGH);
        assertNotNull("First student should be created", student1);

        Thread.sleep(1000); // Wait for assignment

        // Verify student got laptop
        List<Reservation> reservations = client.getActiveReservations();
        assertEquals("Should have one reservation", 1, reservations.size());
        Reservation firstReservation = reservations.get(0);

        // Create another student (should go to queue)
        Student student2 = createTestStudent("Second", "second@test.com", 100031, PerformanceTypeEnum.HIGH);
        assertNotNull("Second student should be created", student2);

        Thread.sleep(1000); // Wait for queue placement

        // Verify second student is in queue
        List<Student> queue = client.getHighPerformanceQueue();
        assertEquals("Should have one student in queue", 1, queue.size());
        assertEquals("Second student should be in queue", student2.getViaId(), queue.get(0).getViaId());

        // Return the laptop
        boolean returned = client.completeReservation(firstReservation.getReservationId());
        assertTrue("Laptop should be returned", returned);

        Thread.sleep(1000); // Wait for laptop state change

        // Process queues
        int processed = client.processQueues();
        assertTrue("At least one assignment should be made from queue", processed > 0);

        Thread.sleep(1000); // Wait for processing

        // Verify queue is now empty and second student has laptop
        queue = client.getHighPerformanceQueue();
        assertEquals("Queue should be empty", 0, queue.size());

        reservations = client.getActiveReservations();
        assertEquals("Should have one active reservation again", 1, reservations.size());
        assertEquals("Second student should now have the laptop",
                student2.getViaId(), reservations.get(0).getStudent().getViaId());

        System.out.println("✓ Queue processed correctly when laptop returned");
    }

    /**
     * Test 5: Verify performance type matching
     */
    @Test
    public void testPerformanceTypeMatching() throws InterruptedException {
        System.out.println("\n=== TEST 5: Performance type matching ===");

        // Create one high and one low laptop
        Laptop highLaptop = client.createLaptop("Dell", "High", 1024, 32, PerformanceTypeEnum.HIGH);
        Laptop lowLaptop = client.createLaptop("HP", "Low", 256, 8, PerformanceTypeEnum.LOW);

        Thread.sleep(500);

        // Create high-performance student
        Student highStudent = createTestStudent("HighNeed", "high@test.com", 100040, PerformanceTypeEnum.HIGH);

        Thread.sleep(1000);

        // Verify high student got high laptop
        List<Reservation> reservations = client.getActiveReservations();
        assertEquals("Should have one reservation", 1, reservations.size());
        assertEquals("Should get high-performance laptop",
                highLaptop.getId(), reservations.get(0).getLaptop().getId());

        // Create low-performance student
        Student lowStudent = createTestStudent("LowNeed", "low@test.com", 100041, PerformanceTypeEnum.LOW);

        Thread.sleep(1000);

        // Verify low student got low laptop
        reservations = client.getActiveReservations();
        assertEquals("Should have two reservations", 2, reservations.size());

        Reservation lowReservation = reservations.stream()
                .filter(r -> r.getStudent().getViaId() == lowStudent.getViaId())
                .findFirst()
                .orElse(null);

        assertNotNull("Low student should have a reservation", lowReservation);
        assertEquals("Should get low-performance laptop",
                lowLaptop.getId(), lowReservation.getLaptop().getId());

        System.out.println("✓ Performance types matched correctly");
    }

    // Helper methods

    private Student createTestStudent(String name, String email, int baseViaId, PerformanceTypeEnum performanceType) {
        // Add random offset to avoid conflicts
        int uniqueViaId = baseViaId + random.nextInt(100000);
        return client.createStudent(
                name,
                new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000), // 1 year from now
                "Test Degree",
                uniqueViaId,
                email,
                uniqueViaId, // Use VIA ID as phone number
                performanceType
        );
    }

    private void clearAllData() {
        try {
            // Clear all reservations
            List<Reservation> allReservations = client.getAllReservations();
            for (Reservation reservation : allReservations) {
                if (reservation.getStatus() == ReservationStatusEnum.ACTIVE) {
                    client.completeReservation(reservation.getReservationId());
                }
            }

            // Note: We cannot delete students and laptops through the client interface
            // In a real test, you might want to add delete methods or use database cleanup

        } catch (Exception e) {
            System.err.println("Error clearing data: " + e.getMessage());
        }
    }
}