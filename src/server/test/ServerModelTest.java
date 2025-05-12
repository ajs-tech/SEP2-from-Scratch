package server.test;

import enums.PerformanceTypeEnum;
import objects.Laptop;
import objects.Student;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import server.model.ServerModel;
import server.model.ServerModelImpl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Test for ServerModelImpl-klassen.
 * Tester forretningslogikken på server-siden.
 *
 * BEMÆRK: Disse tests kræver en fungerende database-forbindelse.
 */
public class ServerModelTest {
    private ServerModel serverModel;
    private Student testStudent;
    private Laptop testLaptop;

    @Before
    public void setUp() {
        // Opret en ny ServerModel for testene
        serverModel = new ServerModelImpl();

        // Opret testdata
        createTestData();
    }

    @After
    public void tearDown() {
        // Fjern testdata
        cleanup();
    }

    /**
     * Opretter testdata der bruges i tests
     */
    private void createTestData() {
        try {
            // Opret teststudent med unikke data (brug timestamp i email for at undgå duplikater)
            long timestamp = System.currentTimeMillis();
            testStudent = serverModel.createStudent(
                    "Test Student",
                    new Date(System.currentTimeMillis() + 31536000000L), // 1 år fra nu
                    "Computer Science Test",
                    999999, // Testbruger-id
                    "teststudent" + timestamp + "@test.com",
                    12345678,
                    PerformanceTypeEnum.HIGH
            );

            // Opret testlaptop
            testLaptop = serverModel.createLaptop(
                    "Test Brand",
                    "Test Model",
                    500, // GB
                    16,  // RAM
                    PerformanceTypeEnum.HIGH
            );

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
                serverModel.deleteStudent(testStudent.getViaId());
            }

            // Fjern testlaptop
            if (testLaptop != null) {
                serverModel.deleteLaptop(testLaptop.getId());
            }
        } catch (Exception e) {
            System.err.println("Fejl ved oprydning: " + e.getMessage());
        }
    }

    @Test
    public void testCreateAndGetStudent() {
        // Test at teststudenten blev oprettet
        assertNotNull("Teststudent bør være oprettet", testStudent);

        // Hent student fra model
        Student retrievedStudent = serverModel.getStudentByID(testStudent.getViaId());

        // Kontroller at student blev hentet
        assertNotNull("Student bør kunne hentes fra model", retrievedStudent);
        assertEquals("Student VIA ID bør matche", testStudent.getViaId(), retrievedStudent.getViaId());
        assertEquals("Student navn bør matche", testStudent.getName(), retrievedStudent.getName());
    }

    @Test
    public void testCreateAndGetLaptop() {
        // Test at testlaptop blev oprettet
        assertNotNull("Testlaptop bør være oprettet", testLaptop);

        // Hent laptop fra model
        Laptop retrievedLaptop = serverModel.getLaptopByUUID(testLaptop.getId());

        // Kontroller at laptop blev hentet
        assertNotNull("Laptop bør kunne hentes fra model", retrievedLaptop);
        assertEquals("Laptop ID bør matche", testLaptop.getId(), retrievedLaptop.getId());
        assertEquals("Laptop mærke bør matche", testLaptop.getBrand(), retrievedLaptop.getBrand());
        assertEquals("Laptop model bør matche", testLaptop.getModel(), retrievedLaptop.getModel());
    }

    @Test
    public void testGetAvailableLaptops() {
        // Hent tilgængelige laptops
        List<Laptop> availableLaptops = new ArrayList<>();
        availableLaptops = serverModel.getAvailableLaptops();

        // Tjek at listen ikke er null
        assertNotNull("Listen af tilgængelige laptops bør ikke være null", availableLaptops);

        // Testlaptop bør være tilgængelig (hvis den blev oprettet)
        if (testLaptop != null) {
            boolean found = false;
            for (Laptop laptop : availableLaptops) {
                if (laptop.getId().equals(testLaptop.getId())) {
                    found = true;
                    break;
                }
            }
            assertTrue("Testlaptop bør være i listen af tilgængelige laptops", found);
        }
    }

    @Test
    public void testServerModelEvents() throws InterruptedException {
        // Opret en countdown latch for at vente på event
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] eventReceived = {false};

        // Tilføj en listener til serverModel
        PropertyChangeListener listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("server_laptop_created".equals(evt.getPropertyName())) {
                    eventReceived[0] = true;
                    latch.countDown();
                }
            }
        };

        serverModel.addListener(listener);

        // Opret en ny laptop for at udløse event
        Laptop newLaptop = serverModel.createLaptop(
                "Event Test Brand",
                "Event Test Model",
                250, // GB
                8,   // RAM
                PerformanceTypeEnum.LOW
        );

        // Vent på at modtage eventet med timeout
        boolean received = latch.await(5, TimeUnit.SECONDS);

        // Fjern listener
        serverModel.removeListener(listener);

        // Opryd efter testen
        if (newLaptop != null) {
            serverModel.deleteLaptop(newLaptop.getId());
        }

        assertTrue("ServerModel burde affyre events", received && eventReceived[0]);
    }

    @Test
    public void testCreateReservation() {
        // Dette test forudsætter at både student og laptop er oprettet
        if (testStudent == null || testLaptop == null) {
            fail("Kan ikke teste reservation uden gyldig student og laptop");
            return;
        }

        // Opret en reservation
        try {
            // Skift laptop til tilgængelig tilstand hvis den ikke er det
            if (!testLaptop.isAvailable()) {
                testLaptop = serverModel.updateLaptopState(testLaptop.getId());
            }

            // Opret reservation
            boolean success = serverModel.canAssignLaptop(testStudent.getPerformanceNeeded());
            assertTrue("Det burde være muligt at reservere en laptop", success);

            // Hvis det er muligt, opret faktisk reservationen
            if (success) {
                // Opret reservation
                serverModel.createReservation(testStudent, testLaptop);

                // Tjek at laptop ikke længere er tilgængelig
                Laptop updatedLaptop = serverModel.getLaptopByUUID(testLaptop.getId());
                assertFalse("Laptop bør ikke være tilgængelig efter reservation",
                        updatedLaptop.isAvailable());
                assertTrue("Laptop bør være udlånt efter reservation",
                        updatedLaptop.isLoaned());
            }
        } catch (Exception e) {
            fail("Fejl ved test af reservation: " + e.getMessage());
        }
    }
}