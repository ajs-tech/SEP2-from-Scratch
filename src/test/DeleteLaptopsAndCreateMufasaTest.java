package test;

import client.network.SocketClient;
import client.network.SocketClientImp;
import enums.PerformanceTypeEnum;
import objects.Laptop;
import objects.Student;
import org.junit.*;
import server.model.ServerModel;
import server.model.ServerModelImpl;
import server.network.SocketServer;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Test der sletter alle tilgængelige laptops fra systemet
 * og opretter en ny studerende ved navn "Mufasa".
 */
public class DeleteLaptopsAndCreateMufasaTest {
    private static SocketServer server;
    private static ServerModel serverModel;
    private static final int TEST_PORT = 9993;
    private SocketClient client;
    private Random random = new Random();

    // Midlertidige data til at blive slettet efter test
    private Student mufasaStudent;
    private List<UUID> deletedLaptopIds;

    @BeforeClass
    public static void setUpClass() {
        try {
            // Start server for testen
            serverModel = new ServerModelImpl();
            server = new SocketServer(serverModel, TEST_PORT);
            server.startServer();
            Thread.sleep(1000); // Vent på at serveren starter
            System.out.println("Test server startet på port " + TEST_PORT);
        } catch (Exception e) {
            fail("Kunne ikke starte server: " + e.getMessage());
        }
    }

    @AfterClass
    public static void tearDownClass() {
        if (server != null && server.isRunning()) {
            server.stopServer();
            System.out.println("Test server lukket");
        }
    }

    @Before
    public void setUp() {
        try {
            // Opret klient forbindelse
            client = new SocketClientImp("localhost", TEST_PORT);
            Thread.sleep(500); // Vent på forbindelse
            System.out.println("Klient forbundet til server");

            // Initialiser lister
            deletedLaptopIds = new ArrayList<>();

        } catch (Exception e) {
            fail("Kunne ikke opsætte test: " + e.getMessage());
        }
    }

    @After
    public void tearDown() {
        // Ryd op efter testen
        cleanup();

        if (client instanceof SocketClientImp) {
            ((SocketClientImp) client).disconnect();
            System.out.println("Klient afbrudt fra server");
        }
    }

    /**
     * Tester sletning af alle tilgængelige laptops og oprettelse af en ny studerende ved navn "Mufasa"
     */
    @Test
    public void testDeleteAllAvailableLaptopsAndCreateMufasa() throws InterruptedException {
        System.out.println("\n=== TEST: Delete Available Laptops and Create Mufasa ===");

        // Trin 1: Tjek status før test
        List<Laptop> laptopsBefore = client.getAvailableLaptops();
        int availableBefore = laptopsBefore.size();

        System.out.println("Antal tilgængelige laptops før sletning: " + availableBefore);
        for (Laptop laptop : laptopsBefore) {
            System.out.println(" - Laptop: " + laptop.getBrand() + " " + laptop.getModel() +
                    " (ID: " + laptop.getId() + ")");
        }

        // Trin 2: Slet alle tilgængelige laptops
        for (Laptop laptop : laptopsBefore) {
            UUID laptopId = laptop.getId();
            client.deleteLaptop(laptopId);
            deletedLaptopIds.add(laptopId);
            System.out.println("Slettet laptop: " + laptop.getBrand() + " " + laptop.getModel() +
                    " (ID: " + laptopId + ")");
        }

        // Vent på at alle laptops bliver slettet
        Thread.sleep(1000);

        // Trin 3: Verificer at alle tilgængelige laptops er slettet
        List<Laptop> laptopsAfter = client.getAvailableLaptops();
        int availableAfter = laptopsAfter.size();

        System.out.println("Antal tilgængelige laptops efter sletning: " + availableAfter);
        assertEquals("Alle tilgængelige laptops skulle være slettet", 0, availableAfter);

        // Trin 4: Opret ny studerende ved navn "Mufasa"
        int viaId = generateUniqueViaId();
        mufasaStudent = client.createStudent(
                "Mufasa",
                new Date(System.currentTimeMillis() + 31536000000L), // 1 år frem
                "Jungle Management",
                viaId,
                "mufasa.king" + viaId + "@priderock.com",
                87654321,
                PerformanceTypeEnum.HIGH // Kongen skal have en high-performance laptop
        );

        assertNotNull("Studerende 'Mufasa' skulle kunne oprettes", mufasaStudent);
        assertEquals("Studerende skulle have navnet 'Mufasa'", "Mufasa", mufasaStudent.getName());
        assertEquals("Studerende skulle have high-performance behov",
                PerformanceTypeEnum.HIGH, mufasaStudent.getPerformanceNeeded());

        System.out.println("Oprettet studerende: " + mufasaStudent.getName() +
                " (VIA ID: " + mufasaStudent.getViaId() + ")");

        // Trin 5: Da der ikke er nogen tilgængelige laptops, bør Mufasa være i køen
        // Vent på at studerende bliver placeret i kø
        Thread.sleep(2000);

        // Tjek at Mufasa er i high-performance køen
        List<Student> highQueue = client.getHighPerformanceQueue();
        assertNotNull("High-performance kø skulle findes", highQueue);

        System.out.println("Antal studerende i high-performance kø: " + highQueue.size());
        for (Student student : highQueue) {
            System.out.println(" - Student i kø: " + student.getName() + " (VIA ID: " + student.getViaId() + ")");
        }

        boolean mufasaInQueue = false;
        for (Student student : highQueue) {
            if (student.getViaId() == mufasaStudent.getViaId()) {
                mufasaInQueue = true;
                System.out.println("Fandt Mufasa i high-performance køen!");
                break;
            }
        }

        assertTrue("Mufasa skulle være placeret i high-performance køen", mufasaInQueue);

        // Verificer at ReservationData også har opdateret køen korrekt (intern implementationsdetalje)
        // Dette ville normalt kræve adgang til modellen, som vi ikke har i testen
        // Derfor verificerer vi kun via client API

        System.out.println("✅ SUCCES: Alle tilgængelige laptops slettet og Mufasa oprettet og placeret i kø");
    }

    // ======= Hjælpemetoder =======

    /**
     * Genererer et unikt VIA ID til test
     */
    private int generateUniqueViaId() {
        return 500000 + random.nextInt(499999); // Generate in range 500000-999999
    }

    /**
     * Rydder op efter testen
     */
    private void cleanup() {
        try {
            // Slet Mufasa hvis han findes
            if (mufasaStudent != null) {
                client.deleteStudent(mufasaStudent.getViaId());
                System.out.println("Slettet studerende: " + mufasaStudent.getName());
                mufasaStudent = null;
            }

            System.out.println("Oprydning gennemført");
        } catch (Exception e) {
            System.err.println("Fejl ved oprydning: " + e.getMessage());
        }
    }
}