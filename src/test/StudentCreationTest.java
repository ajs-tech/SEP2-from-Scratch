package test;

import enums.PerformanceTypeEnum;
import model.Model;
import model.ModelImpl;
import objects.Laptop;
import objects.Reservation;
import objects.Student;
import org.junit.Before;
import org.junit.Test;
import server.model.ServerModel;
import server.model.ServerModelImpl;
import server.network.SocketServer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Tests for the student creation functionality, including automatic laptop assignment
 * and queue placement when no laptops are available.
 */
public class StudentCreationTest {

    private static final int TEST_PORT = 9995;
    private static SocketServer server;
    private static ServerModel serverModel;
    private Model modelImp;

    // Test data
    private Random random = new Random();
    private List<Laptop> tempLaptops = new ArrayList<>();
    private List<Student> tempStudents = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        // Start server if not already running
        if (server == null || !server.isRunning()) {
            serverModel = new ServerModelImpl();
            server = new SocketServer(serverModel, TEST_PORT);
            server.startServer();

            // Allow time for server to start
            Thread.sleep(1000);
        }

        // Get model instance
        modelImp = ModelImpl.getInstance();

        // Wait to ensure client connection is established
        Thread.sleep(500);
    }

    /**
     * Generates a unique VIA ID for testing
     */
    private int generateUniqueViaId() {
        return 100000 + random.nextInt(900000);
    }

    /**
     * Creates a test laptop with the specified performance type
     */
    private Laptop createTestLaptop(PerformanceTypeEnum performanceType) {
        String brand = "Test Brand";
        String model = "Test Model " + System.currentTimeMillis();
        int ram = performanceType == PerformanceTypeEnum.HIGH ? 32 : 8;
        int diskSpace = performanceType == PerformanceTypeEnum.HIGH ? 1000 : 500;

        Laptop laptop = modelImp.createLaptop(brand, model, diskSpace, ram, performanceType);
        if (laptop != null) {
            tempLaptops.add(laptop);
        }
        return laptop;
    }

    /**
     * Creates a test student with specified performance needs
     */
    private Student createTestStudent(PerformanceTypeEnum performanceNeeded) {
        int viaId = generateUniqueViaId();
        String name = "Test Student " + viaId;
        String email = "test" + viaId + "@example.com";
        int phoneNumber = 10000000 + random.nextInt(90000000);
        String degreeTitle = "Test Degree";
        Date degreeEndDate = new Date(System.currentTimeMillis() + 31536000000L); // 1 year from now

        Student student = modelImp.createStudent(name, degreeEndDate, degreeTitle, viaId, email, phoneNumber, performanceNeeded);
        if (student != null) {
            tempStudents.add(student);
        }
        return student;
    }

    /**
     * Cleans up any test data created during the test
     */
    private void cleanupTestData() {
        System.out.println("Cleaning up test data...");

        // Delete temporary students
        for (Student student : tempStudents) {
            try {
                modelImp.deleteStudent(student.getViaId());
                System.out.println("Deleted student: " + student.getName());
            } catch (Exception e) {
                System.err.println("Failed to delete student: " + student.getViaId() + " - " + e.getMessage());
            }
        }
        tempStudents.clear();

        // Delete temporary laptops
        for (Laptop laptop : tempLaptops) {
            try {
                modelImp.deleteLaptop(laptop.getId());
                System.out.println("Deleted laptop: " + laptop.getBrand() + " " + laptop.getModel());
            } catch (Exception e) {
                System.err.println("Failed to delete laptop: " + laptop.getId() + " - " + e.getMessage());
            }
        }
        tempLaptops.clear();
    }

    /**
     * Tests that a student is automatically assigned a laptop when one is available
     */
    @Test
    public void testStudentCreationWithAvailableLaptop() throws Exception {
        try {
            System.out.println("Running testStudentCreationWithAvailableLaptop");

            // Create a laptop with HIGH performance
            PerformanceTypeEnum laptopType = PerformanceTypeEnum.HIGH;
            Laptop testLaptop = createTestLaptop(laptopType);

            assertNotNull("Failed to create test laptop", testLaptop);
            System.out.println("Created test laptop: " + testLaptop.getBrand() + " " + testLaptop.getModel());

            // Ensure it's available
            modelImp.updateLaptopState(testLaptop.getId()); // Toggle state if needed

            // Create a student with the same performance needs
            Student testStudent = createTestStudent(laptopType);

            assertNotNull("Failed to create test student", testStudent);
            System.out.println("Created test student: " + testStudent.getName() + " (VIA ID: " + testStudent.getViaId() + ")");

            // Wait for the server to process the reservation
            Thread.sleep(1000);

            // Verify that a reservation was created
            boolean foundReservation = false;
            List<Reservation> activeReservations = modelImp.getActiveReservations();

            assertNotNull("Failed to get active reservations", activeReservations);
            System.out.println("Found " + activeReservations.size() + " active reservations");

            for (Reservation reservation : activeReservations) {
                if (reservation.getStudent().getViaId() == testStudent.getViaId()) {
                    foundReservation = true;
                    System.out.println("Found reservation for student: " +
                            testStudent.getName() + " with laptop: " +
                            reservation.getLaptop().getBrand() + " " +
                            reservation.getLaptop().getModel());
                    break;
                }
            }

            assertTrue("No reservation was created for the student", foundReservation);
        } finally {
            cleanupTestData();
        }
    }

    /**
     * Tests that a student is added to the appropriate queue when no matching laptops are available
     */
    @Test
    public void testStudentCreationWithNoAvailableLaptops() throws Exception {
        try {
            System.out.println("Running testStudentCreationWithNoAvailableLaptops");

            // First, make all existing high performance laptops unavailable by creating reservations for them
            List<Laptop> availableLaptops = modelImp.getAvailableLaptops();
            System.out.println("Available laptops before test: " + availableLaptops.size());

            // Check current high-performance laptops
            List<Laptop> highPerformanceLaptops = new ArrayList<>();
            for (Laptop laptop : availableLaptops) {
                if (laptop.getPerformanceType() == PerformanceTypeEnum.HIGH && laptop.isAvailable()) {
                    highPerformanceLaptops.add(laptop);
                }
            }

            System.out.println("Available HIGH performance laptops: " + highPerformanceLaptops.size());

            // Use up all available high-performance laptops
            for (Laptop laptop : highPerformanceLaptops) {
                Student tempStudent = createTestStudent(PerformanceTypeEnum.HIGH);
                System.out.println("Created temporary student to use laptop: " + tempStudent.getName());

                Reservation reservation = modelImp.createReservation(tempStudent, laptop);
                assertNotNull("Failed to create reservation for temporary student", reservation);

                System.out.println("Created reservation for laptop: " + laptop.getBrand() + " " + laptop.getModel());
            }

            // Verify no high-performance laptops are available
            availableLaptops = modelImp.getAvailableLaptops();
            boolean highPerformanceAvailable = false;
            for (Laptop laptop : availableLaptops) {
                if (laptop.getPerformanceType() == PerformanceTypeEnum.HIGH && laptop.isAvailable()) {
                    highPerformanceAvailable = true;
                    System.out.println("Found available high-performance laptop: " +
                            laptop.getBrand() + " " + laptop.getModel());
                    break;
                }
            }

            if (highPerformanceAvailable) {
                System.out.println("WARNING: Could not make all high-performance laptops unavailable");
            } else {
                System.out.println("Successfully made all high-performance laptops unavailable");
            }

            // Create a student with HIGH performance needs
            PerformanceTypeEnum studentType = PerformanceTypeEnum.HIGH;
            Student testStudent = createTestStudent(studentType);

            assertNotNull("Failed to create test student", testStudent);
            System.out.println("Created test student with HIGH performance needs: " +
                    testStudent.getName() + " (VIA ID: " + testStudent.getViaId() + ")");

            // Wait for server to process
            Thread.sleep(2000);

            // Check if student is in high performance queue
            List<Student> highQueue = modelImp.getHighPerformanceQueue();

            assertNotNull("Failed to get high performance queue", highQueue);
            System.out.println("High performance queue size: " + highQueue.size());

            boolean inQueue = false;
            for (Student student : highQueue) {
                System.out.println("Student in queue: " + student.getName() + " (VIA ID: " + student.getViaId() + ")");
                if (student.getViaId() == testStudent.getViaId()) {
                    inQueue = true;
                    System.out.println("Found student in high performance queue");
                    break;
                }
            }

            // Also check if the student has a reservation (which would mean a laptop became available)
            boolean hasReservation = false;
            List<Reservation> activeReservations = modelImp.getActiveReservations();
            for (Reservation reservation : activeReservations) {
                if (reservation.getStudent().getViaId() == testStudent.getViaId()) {
                    hasReservation = true;
                    System.out.println("Student was assigned a laptop (one became available): " +
                            reservation.getLaptop().getBrand() + " " + reservation.getLaptop().getModel());
                    break;
                }
            }

            // The student should either be in the queue or have a reservation
            assertTrue("Student was neither added to the queue nor assigned a laptop", inQueue || hasReservation);

        } finally {
            cleanupTestData();
        }
    }

    /**
     * Tests creating a student with low performance needs when no matching laptops are available
     */
    @Test
    public void testLowPerformanceStudentWithNoAvailableLaptops() throws Exception {
        try {
            System.out.println("Running testLowPerformanceStudentWithNoAvailableLaptops");

            // First, make all existing low performance laptops unavailable
            List<Laptop> availableLaptops = modelImp.getAvailableLaptops();
            System.out.println("Available laptops before test: " + availableLaptops.size());

            // Check current low-performance laptops
            List<Laptop> lowPerformanceLaptops = new ArrayList<>();
            for (Laptop laptop : availableLaptops) {
                if (laptop.getPerformanceType() == PerformanceTypeEnum.LOW && laptop.isAvailable()) {
                    lowPerformanceLaptops.add(laptop);
                }
            }

            System.out.println("Available LOW performance laptops: " + lowPerformanceLaptops.size());

            // Use up all available low-performance laptops
            for (Laptop laptop : lowPerformanceLaptops) {
                Student tempStudent = createTestStudent(PerformanceTypeEnum.LOW);
                System.out.println("Created temporary student to use laptop: " + tempStudent.getName());

                Reservation reservation = modelImp.createReservation(tempStudent, laptop);
                assertNotNull("Failed to create reservation for temporary student", reservation);

                System.out.println("Created reservation for laptop: " + laptop.getBrand() + " " + laptop.getModel());
            }

            // Verify no low-performance laptops are available
            availableLaptops = modelImp.getAvailableLaptops();
            boolean lowPerformanceAvailable = false;
            for (Laptop laptop : availableLaptops) {
                if (laptop.getPerformanceType() == PerformanceTypeEnum.LOW && laptop.isAvailable()) {
                    lowPerformanceAvailable = true;
                    System.out.println("Found available low-performance laptop: " +
                            laptop.getBrand() + " " + laptop.getModel());
                    break;
                }
            }

            if (lowPerformanceAvailable) {
                System.out.println("WARNING: Could not make all low-performance laptops unavailable");
            } else {
                System.out.println("Successfully made all low-performance laptops unavailable");
            }

            // Create a student with LOW performance needs
            PerformanceTypeEnum studentType = PerformanceTypeEnum.LOW;
            Student testStudent = createTestStudent(studentType);

            assertNotNull("Failed to create test student", testStudent);
            System.out.println("Created test student with LOW performance needs: " +
                    testStudent.getName() + " (VIA ID: " + testStudent.getViaId() + ")");

            // Wait for server to process
            Thread.sleep(1000);

            // Check if student is in low performance queue
            List<Student> lowQueue = modelImp.getLowPerformanceQueue();

            assertNotNull("Failed to get low performance queue", lowQueue);
            System.out.println("Low performance queue size: " + lowQueue.size());

            boolean inQueue = false;
            for (Student student : lowQueue) {
                System.out.println("Student in queue: " + student.getName() + " (VIA ID: " + student.getViaId() + ")");
                if (student.getViaId() == testStudent.getViaId()) {
                    inQueue = true;
                    System.out.println("Found student in low performance queue");
                    break;
                }
            }

            // Also check if the student has a reservation (which would mean a laptop became available)
            boolean hasReservation = false;
            List<Reservation> activeReservations = modelImp.getActiveReservations();
            for (Reservation reservation : activeReservations) {
                if (reservation.getStudent().getViaId() == testStudent.getViaId()) {
                    hasReservation = true;
                    System.out.println("Student was assigned a laptop (one became available): " +
                            reservation.getLaptop().getBrand() + " " + reservation.getLaptop().getModel());
                    break;
                }
            }

            // The student should either be in the queue or have a reservation
            assertTrue("Student was neither added to the queue nor assigned a laptop", inQueue || hasReservation);

        } finally {
            cleanupTestData();
        }
    }

    /**
     * Comprehensive test that creates students with and without available laptops
     */
    @Test
    public void testMixedStudentCreation() throws Exception {
        try {
            System.out.println("Running testMixedStudentCreation");

            // Step 1: Create a high-performance laptop
            Laptop highLaptop = createTestLaptop(PerformanceTypeEnum.HIGH);
            assertNotNull("Failed to create high-performance laptop", highLaptop);
            System.out.println("Created high-performance laptop: " + highLaptop.getBrand() + " " + highLaptop.getModel());

            // Step 2: Create a student with high-performance needs - should get the laptop
            Student student1 = createTestStudent(PerformanceTypeEnum.HIGH);
            assertNotNull("Failed to create first student", student1);
            System.out.println("Created first student (high perf needs): " + student1.getName());

            // Wait for processing
            Thread.sleep(1000);

            // Step 3: Create another student with high-performance needs - should go to queue
            Student student2 = createTestStudent(PerformanceTypeEnum.HIGH);
            assertNotNull("Failed to create second student", student2);
            System.out.println("Created second student (high perf needs): " + student2.getName());

            // Wait for processing
            Thread.sleep(1000);

            // Check if student1 got a laptop
            boolean student1HasLaptop = false;
            List<Reservation> reservations = modelImp.getActiveReservations();
            for (Reservation reservation : reservations) {
                if (reservation.getStudent().getViaId() == student1.getViaId()) {
                    student1HasLaptop = true;
                    System.out.println("Student 1 got laptop: " +
                            reservation.getLaptop().getBrand() + " " + reservation.getLaptop().getModel());
                    break;
                }
            }

            // Check if student2 is in the queue
            boolean student2InQueue = false;
            List<Student> highQueue = modelImp.getHighPerformanceQueue();
            for (Student student : highQueue) {
                if (student.getViaId() == student2.getViaId()) {
                    student2InQueue = true;
                    System.out.println("Student 2 was added to high-performance queue");
                    break;
                }
            }

            // Also check if student2 got a laptop (shouldn't happen, but check anyway)
            boolean student2HasLaptop = false;
            for (Reservation reservation : reservations) {
                if (reservation.getStudent().getViaId() == student2.getViaId()) {
                    student2HasLaptop = true;
                    System.out.println("Student 2 unexpectedly got a laptop: " +
                            reservation.getLaptop().getBrand() + " " + reservation.getLaptop().getModel());
                    break;
                }
            }

            assertTrue("First student should have been assigned a laptop", student1HasLaptop);
            assertTrue("Second student should either be in queue or have a laptop", student2InQueue || student2HasLaptop);

        } finally {
            cleanupTestData();
        }
    }
}