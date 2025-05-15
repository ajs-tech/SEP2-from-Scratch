package test;

import client.network.SocketClient;
import client.network.SocketClientImp;
import enums.PerformanceTypeEnum;
import objects.Laptop;
import objects.Reservation;
import objects.Student;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import server.model.ServerModel;
import server.model.ServerModelImpl;
import server.network.SocketServer;

import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Test demonstrating queue placement when no laptops are available.
 * This test:
 * 1. Deletes all available laptops from the database
 * 2. Creates a student named Jacob
 * 3. Verifies Jacob is placed in the appropriate queue
 */
public class QueuePlacementTest {
    private static SocketServer server;
    private static ServerModel serverModel;
    private static final int TEST_PORT = 9996;
    private SocketClient client;
    private Random random = new Random();

    @Before
    public void setUp() {
        try {
            // Start server for tests
            serverModel = new ServerModelImpl();
            server = new SocketServer(serverModel, TEST_PORT);
            server.startServer();
            Thread.sleep(1000);
            System.out.println("Test server started on port " + TEST_PORT);

            // Create client connection
            client = new SocketClientImp("localhost", TEST_PORT);
            Thread.sleep(500);
            System.out.println("Client connected to server");
        } catch (Exception e) {
            fail("Could not set up test: " + e.getMessage());
        }
    }

    @After
    public void tearDown() {
        if (client instanceof SocketClientImp) {
            ((SocketClientImp) client).disconnect();
            System.out.println("Client disconnected");
        }

        if (server != null && server.isRunning()) {
            server.stopServer();
            System.out.println("Server stopped");
        }
    }

    @Test
    public void testJacobPlacedInQueue() throws InterruptedException {
        System.out.println("\n=== TEST: Student Jacob Placed In Queue ===");

        // Step 1: Check if there are any available laptops
        List<Laptop> availableLaptops = client.getAvailableLaptops();
        System.out.println("Initially available laptops: " + availableLaptops.size());

        // Step 2: Make all available laptops unavailable by creating reservations for them
        // We'll create temporary students to borrow all laptops
        int count = 0;
        for (Laptop laptop : availableLaptops) {
            int tempViaId = 900000 + random.nextInt(99999);
            Student tempStudent = client.createStudent(
                    "TempStudent" + count++,
                    new Date(System.currentTimeMillis() + 31536000000L), // 1 year from now
                    "Temp Reservation",
                    tempViaId,
                    "temp" + tempViaId + "@example.com",
                    tempViaId, // Use VIA ID as phone number
                    laptop.getPerformanceType()
            );

            // If create student didn't automatically create a reservation, create one manually
            List<Reservation> activeRes = client.getActiveReservations();
            boolean hasReservation = false;
            for (Reservation res : activeRes) {
                if (res.getStudent().getViaId() == tempStudent.getViaId()) {
                    hasReservation = true;
                    break;
                }
            }

            if (!hasReservation) {
                client.createReservation(tempStudent, laptop);
            }

            Thread.sleep(200); // Small delay to avoid overwhelming server
        }

        // Step 3: Verify no laptops are available
        availableLaptops = client.getAvailableLaptops();
        System.out.println("Available laptops after reservations: " + availableLaptops.size());

        if (availableLaptops.size() > 0) {
            System.out.println("WARNING: Could not make all laptops unavailable. Some laptops may still be available.");
        }

        // Step 4: Now create student Jacob with HIGH performance needs
        // Choose a performance type - using HIGH for this test
        PerformanceTypeEnum performanceType = PerformanceTypeEnum.HIGH;

        int jacobViaId = 123456; // Choose a simple ID for Jacob
        Student jacob = client.createStudent(
                "Jacob",
                new Date(System.currentTimeMillis() + 31536000000L), // 1 year from now
                "Computer Science",
                jacobViaId,
                "jacob@example.com",
                87654321, // Phone number
                performanceType
        );

        assertNotNull("Jacob should be created successfully", jacob);
        System.out.println("Created student Jacob with VIA ID: " + jacobViaId +
                " and performance type: " + performanceType);

        // Step 5: Wait for server to process and place Jacob in queue
        Thread.sleep(2000);

        // Step 6: Check if Jacob is in the appropriate queue
        List<Student> queue;
        if (performanceType == PerformanceTypeEnum.HIGH) {
            queue = client.getHighPerformanceQueue();
            System.out.println("Checking high-performance queue for Jacob...");
        } else {
            queue = client.getLowPerformanceQueue();
            System.out.println("Checking low-performance queue for Jacob...");
        }

        boolean jacobInQueue = false;
        for (Student student : queue) {
            System.out.println("Student in queue: " + student.getName() + " (VIA ID: " + student.getViaId() + ")");
            if (student.getViaId() == jacobViaId) {
                jacobInQueue = true;
                System.out.println("✅ Found Jacob in the queue!");
                break;
            }
        }

        assertTrue("Jacob should be placed in the queue since no laptops are available", jacobInQueue);

        if (jacobInQueue) {
            System.out.println("✅ TEST PASSED: Jacob was successfully placed in the " +
                    (performanceType == PerformanceTypeEnum.HIGH ? "high" : "low") +
                    "-performance queue!");
        } else {
            System.out.println("❌ TEST FAILED: Jacob was not found in the queue");

            // Check if Jacob was assigned a laptop instead (shouldn't happen as we made all unavailable)
            List<Reservation> allReservations = client.getActiveReservations();
            for (Reservation res : allReservations) {
                if (res.getStudent().getViaId() == jacobViaId) {
                    System.out.println("⚠️ Jacob was unexpectedly assigned a laptop: " +
                            res.getLaptop().getBrand() + " " + res.getLaptop().getModel());
                    break;
                }
            }
        }
    }
}