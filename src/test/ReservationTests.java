package test;

import client.network.SocketClient;
import client.network.SocketClientImp;
import enums.PerformanceTypeEnum;
import enums.ReservationStatusEnum;
import model.Model;
import model.ModelImpl;
import objects.Laptop;
import objects.Reservation;
import objects.Student;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import server.model.ServerModel;
import server.model.ServerModelImpl;
import server.network.SocketServer;
import viewmodel.ReturnLaptopViewModel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Comprehensive tests for reservation functionality including database, server, and client.
 */
public class ReservationTests {
    private static SocketServer server;
    private static ServerModel serverModel;
    private static final int TEST_PORT = 9995;
    private SocketClient client;
    private Model model;
    private Student testStudent;
    private Laptop testLaptop;
    private Connection dbConnection;

    // Use Random to generate unique IDs for each test run
    private static final Random random = new Random();

    @Before
    public void setUp() {
        try {
            // Start server
            System.out.println("Starting server on port " + TEST_PORT);
            serverModel = new ServerModelImpl();
            server = new SocketServer(serverModel, TEST_PORT);
            server.startServer();

            // Wait for server to start
            Thread.sleep(500);

            // Connect client
            System.out.println("Connecting client to server");
            client = new SocketClientImp("localhost", TEST_PORT);

            // Get model instance (use reflection to set the client)
            model = ModelImpl.getInstance();

            // Wait for client to connect
            Thread.sleep(500);

            // Establish direct database connection for verification
            System.out.println("Establishing database connection");
            setupDatabaseConnection();

            // Create test data
            System.out.println("Creating test data");
            createTestData();

        } catch (Exception e) {
            System.err.println("Error in test setup: " + e.getMessage());
            e.printStackTrace();
            fail("Test setup failed: " + e.getMessage());
        }
    }

    private void setupDatabaseConnection() {
        try {
            Properties props = new Properties();
            props.setProperty("user", "neondb_owner");
            props.setProperty("password", "npg_6oHRbjLDgK8t");
            props.setProperty("ssl", "require");

            dbConnection = DriverManager.getConnection(
                    "jdbc:postgresql://ep-mute-boat-a9rul5u1-pooler.gwc.azure.neon.tech/neondb",
                    props
            );

            System.out.println("Database connection established");
        } catch (Exception e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generate a unique VIA ID to avoid database conflicts
     */
    private int generateUniqueViaId() {
        // Generate a random 6-digit ID between 100000 and 999999
        return 100000 + random.nextInt(900000);
    }

    /**
     * Creates test data for the tests
     */
    private void createTestData() {
        try {
            // Create student
            int uniqueViaId = generateUniqueViaId();
            testStudent = client.createStudent(
                    "Test Student",
                    new Date(System.currentTimeMillis() + 31536000000L), // 1 year from now
                    "Test Program",
                    uniqueViaId,
                    "test" + uniqueViaId + "@test.com",
                    12345678,
                    PerformanceTypeEnum.LOW
            );

            assertNotNull("Failed to create test student", testStudent);
            System.out.println("Created test student with VIA ID: " + testStudent.getViaId());

            // Create laptop
            testLaptop = client.createLaptop(
                    "Test Brand",
                    "Test Model",
                    500, // GB
                    8,   // RAM
                    PerformanceTypeEnum.LOW
            );

            assertNotNull("Failed to create test laptop", testLaptop);
            System.out.println("Created test laptop with ID: " + testLaptop.getId());

            // Wait for data to be created
            Thread.sleep(500);

        } catch (Exception e) {
            System.err.println("Error creating test data: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to create test data: " + e.getMessage());
        }
    }

    @After
    public void tearDown() {
        try {
            // Clean up test data
            System.out.println("Cleaning up test data");
            cleanup();

            // Close database connection
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
                System.out.println("Database connection closed");
            }

            // Disconnect client
            if (client instanceof SocketClientImp) {
                ((SocketClientImp) client).disconnect();
                System.out.println("Client disconnected");
            }

            // Stop server
            if (server != null && server.isRunning()) {
                server.stopServer();
                System.out.println("Server stopped");
            }

        } catch (Exception e) {
            System.err.println("Error in test teardown: " + e.getMessage());
        }
    }

    /**
     * Cleans up test data
     */
    private void cleanup() {
        try {
            // Delete test student if it exists
            if (testStudent != null) {
                client.deleteStudent(testStudent.getViaId());
                System.out.println("Deleted test student");
            }

            // Delete test laptop if it exists
            if (testLaptop != null) {
                client.deleteLaptop(testLaptop.getId());
                System.out.println("Deleted test laptop");
            }

        } catch (Exception e) {
            System.err.println("Error cleaning up test data: " + e.getMessage());
        }
    }

    /**
     * Direct database query to check if active reservations exist
     */
    private boolean activeReservationsExistInDatabase() {
        try {
            String sql = "SELECT COUNT(*) FROM Reservation WHERE status = ?";
            PreparedStatement stmt = dbConnection.prepareStatement(sql);
            stmt.setString(1, ReservationStatusEnum.ACTIVE.name());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Database query found " + count + " active reservations");
                return count > 0;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error checking active reservations in database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Direct database query to check the status values in use
     */
    private void listReservationStatusValues() {
        try {
            String sql = "SELECT DISTINCT status FROM Reservation";
            PreparedStatement stmt = dbConnection.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();
            System.out.println("Reservation status values in database:");
            while (rs.next()) {
                String status = rs.getString(1);
                System.out.println(" - '" + status + "'");
            }
        } catch (Exception e) {
            System.err.println("Error listing reservation status values: " + e.getMessage());
        }
    }

    /**
     * Test creating a reservation and verifying it in the database
     */
    @Test
    public void testCreateReservation() {
        try {
            System.out.println("=== Running testCreateReservation ===");

            // Create reservation
            Reservation reservation = client.createReservation(testStudent, testLaptop);
            assertNotNull("Failed to create reservation", reservation);
            System.out.println("Created reservation with ID: " + reservation.getReservationId());

            // Give the server time to process
            Thread.sleep(500);

            // Verify reservation exists in database
            String sql = "SELECT * FROM Reservation WHERE reservation_uuid = CAST(? AS UUID)";
            PreparedStatement stmt = dbConnection.prepareStatement(sql);
            stmt.setString(1, reservation.getReservationId().toString());

            ResultSet rs = stmt.executeQuery();
            assertTrue("Reservation not found in database", rs.next());

            String status = rs.getString("status");
            System.out.println("Reservation status in database: " + status);
            assertEquals("Reservation status incorrect", ReservationStatusEnum.ACTIVE.name(), status);

            System.out.println("testCreateReservation completed successfully");
        } catch (Exception e) {
            System.err.println("Error in testCreateReservation: " + e.getMessage());
            e.printStackTrace();
            fail("testCreateReservation failed: " + e.getMessage());
        }
    }

    /**
     * Test getting active reservations from the server
     */
    @Test
    public void testGetActiveReservations() {
        try {
            System.out.println("=== Running testGetActiveReservations ===");

            // Create reservation first
            Reservation reservation = client.createReservation(testStudent, testLaptop);
            assertNotNull("Failed to create reservation", reservation);
            System.out.println("Created reservation with ID: " + reservation.getReservationId());

            // Wait for reservation to be created
            Thread.sleep(500);

            // List all status values in database
            listReservationStatusValues();

            // Check directly in database
            boolean reservationsExist = activeReservationsExistInDatabase();
            assertTrue("No active reservations found in database", reservationsExist);

            // Try to get active reservations
            List<Reservation> activeReservations = client.getActiveReservations();

            // Output results
            System.out.println("getActiveReservations returned: " +
                    (activeReservations == null ? "null" : activeReservations.size() + " reservations"));

            if (activeReservations != null && !activeReservations.isEmpty()) {
                for (Reservation r : activeReservations) {
                    System.out.println(" - Reservation ID: " + r.getReservationId() +
                            ", Student: " + r.getStudent().getName() +
                            ", Status: " + r.getStatus());
                }
            }

            assertNotNull("Active reservations list is null", activeReservations);
            assertFalse("Active reservations list is empty", activeReservations.isEmpty());

            System.out.println("testGetActiveReservations completed successfully");
        } catch (Exception e) {
            System.err.println("Error in testGetActiveReservations: " + e.getMessage());
            e.printStackTrace();
            fail("testGetActiveReservations failed: " + e.getMessage());
        }
    }

    /**
     * Test the ReturnLaptopViewModel's ability to load reservations
     */
    @Test
    public void testReturnLaptopViewModel() {
        try {
            System.out.println("=== Running testReturnLaptopViewModel ===");

            // Create reservation first
            Reservation reservation = client.createReservation(testStudent, testLaptop);
            assertNotNull("Failed to create reservation", reservation);
            System.out.println("Created reservation with ID: " + reservation.getReservationId());

            // Wait for reservation to be created
            Thread.sleep(500);

            // Create the view model
            ReturnLaptopViewModel viewModel = new ReturnLaptopViewModel(model);

            // Create a latch to wait for the property change
            final CountDownLatch latch = new CountDownLatch(1);
            final boolean[] foundReservations = {false};

            // Add listener to detect when reservations are loaded
            PropertyChangeListener listener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    int size = viewModel.getActiveLoans().size();
                    System.out.println("ViewModel active loans size changed: " + size);
                    if (size > 0) {
                        foundReservations[0] = true;
                        latch.countDown();
                    }
                }
            };

            // Add the listener
            viewModel.getActiveLoans().addListener((javafx.collections.ListChangeListener<ReturnLaptopViewModel.LoanTableItem>) c -> {
                System.out.println("ActiveLoans list changed. New size: " + c.getList().size());
                if (c.getList().size() > 0) {
                    foundReservations[0] = true;
                    latch.countDown();
                }
            });

            // Refresh the loan list
            viewModel.refreshLoanList();

            // Wait for the property change or timeout
            boolean refreshCompleted = latch.await(5, TimeUnit.SECONDS);

            // Output active loans size
            System.out.println("Final ViewModel active loans size: " + viewModel.getActiveLoans().size());

            if (viewModel.getActiveLoans().size() > 0) {
                System.out.println("Active loans found in view model:");
                for (ReturnLaptopViewModel.LoanTableItem item : viewModel.getActiveLoans()) {
                    System.out.println(" - Student: " + item.getStudentName() +
                            ", VIA ID: " + item.getViaId() +
                            ", Laptop: " + item.getLaptopBrand() + " " + item.getLaptopModel());
                }
            }

            assertTrue("ViewModel did not load any active loans", foundReservations[0]);

            System.out.println("testReturnLaptopViewModel completed successfully");
        } catch (Exception e) {
            System.err.println("Error in testReturnLaptopViewModel: " + e.getMessage());
            e.printStackTrace();
            fail("testReturnLaptopViewModel failed: " + e.getMessage());
        }
    }

    /**
     * Test reservation mapping in ReservationDAO
     */
    @Test
    public void testReservationMapping() {
        try {
            System.out.println("=== Running testReservationMapping ===");

            // Create reservation first
            Reservation reservation = client.createReservation(testStudent, testLaptop);
            assertNotNull("Failed to create reservation", reservation);
            System.out.println("Created reservation with ID: " + reservation.getReservationId());

            // Wait for reservation to be created
            Thread.sleep(500);

            // Verify the database can be accessed and read correctly
            String sql = "SELECT r.reservation_uuid, r.status, " +
                    "s.via_id, s.name, " +
                    "l.laptop_uuid, l.brand, l.model " +
                    "FROM Reservation r " +
                    "JOIN Student s ON r.student_via_id = s.via_id " +
                    "JOIN Laptop l ON r.laptop_uuid = l.laptop_uuid " +
                    "WHERE r.status = ?";

            PreparedStatement stmt = dbConnection.prepareStatement(sql);
            stmt.setString(1, ReservationStatusEnum.ACTIVE.name());

            ResultSet rs = stmt.executeQuery();

            System.out.println("Direct database query for active reservations:");
            int count = 0;
            while (rs.next()) {
                count++;
                String reservationId = rs.getString("reservation_uuid");
                String status = rs.getString("status");
                int viaId = rs.getInt("via_id");
                String studentName = rs.getString("name");
                String laptopId = rs.getString("laptop_uuid");
                String brand = rs.getString("brand");
                String model = rs.getString("model");

                System.out.println(" - Reservation ID: " + reservationId +
                        ", Status: " + status +
                        ", Student: " + studentName + " (VIA ID: " + viaId + ")" +
                        ", Laptop: " + brand + " " + model + " (ID: " + laptopId + ")");
            }

            assertTrue("No active reservations found in database direct query", count > 0);

            System.out.println("testReservationMapping completed successfully");
        } catch (Exception e) {
            System.err.println("Error in testReservationMapping: " + e.getMessage());
            e.printStackTrace();
            fail("testReservationMapping failed: " + e.getMessage());
        }
    }

    /**
     * Test the complete reservation process
     */
    @Test
    public void testCompleteReservationProcess() {
        try {
            System.out.println("=== Running testCompleteReservationProcess ===");

            // 1. Direct check of database connection
            assertNotNull("Database connection is null", dbConnection);
            assertFalse("Database connection is closed", dbConnection.isClosed());

            // 2. Create a reservation
            Reservation reservation = client.createReservation(testStudent, testLaptop);
            assertNotNull("Failed to create reservation", reservation);
            System.out.println("Created reservation with ID: " + reservation.getReservationId());

            // 3. Wait for reservation to be created
            Thread.sleep(500);

            // 4. Check database directly
            boolean reservationsExist = activeReservationsExistInDatabase();
            assertTrue("No active reservations found in database", reservationsExist);

            // 5. Get active reservations from server
            List<Reservation> activeReservations = serverModel.getActiveReservations();
            assertNotNull("Active reservations list from server is null", activeReservations);
            assertFalse("Active reservations list from server is empty", activeReservations.isEmpty());
            System.out.println("Server returned " + activeReservations.size() + " active reservations");

            // 6. Get active reservations from client
            List<Reservation> clientActiveReservations = client.getActiveReservations();
            assertNotNull("Active reservations list from client is null", clientActiveReservations);
            assertFalse("Active reservations list from client is empty", clientActiveReservations.isEmpty());
            System.out.println("Client returned " + clientActiveReservations.size() + " active reservations");

            // 7. Get active reservations from model
            List<Reservation> modelActiveReservations = model.getActiveReservations();
            assertNotNull("Active reservations list from model is null", modelActiveReservations);
            assertFalse("Active reservations list from model is empty", modelActiveReservations.isEmpty());
            System.out.println("Model returned " + modelActiveReservations.size() + " active reservations");

            // 8. Load the view model
            ReturnLaptopViewModel viewModel = new ReturnLaptopViewModel(model);
            viewModel.refreshLoanList();

            // Wait a moment for async operations
            Thread.sleep(1000);

            // 9. Check the size of the active loans list
            int viewModelSize = viewModel.getActiveLoans().size();
            System.out.println("ViewModel has " + viewModelSize + " active loans");
            assertTrue("ViewModel active loans list is empty", viewModelSize > 0);

            System.out.println("testCompleteReservationProcess completed successfully");
        } catch (Exception e) {
            System.err.println("Error in testCompleteReservationProcess: " + e.getMessage());
            e.printStackTrace();
            fail("testCompleteReservationProcess failed: " + e.getMessage());
        }
    }
}